# Dashboard - Conexões em Tempo Real

## 📊 Visão Geral

O dashboard agora busca dados de conexões **em tempo real** diretamente dos servidores Mikrotik, ao invés de consultar apenas o banco de dados local. Isso garante que os números refletem o estado atual da rede.

## 🏗️ Arquitetura

### Princípios Aplicados

1. **Separação de Responsabilidades**
   - Dados de cadastro → Banco de dados (rápido, transacional)
   - Dados de conexão → Mikrotik (tempo real, eventual consistency)

2. **Proteção Transacional**
   - ✅ Consultas ao banco acontecem dentro de `@Transactional(readOnly = true)`
   - ✅ Chamadas externas ao Mikrotik acontecem **FORA** da transação
   - ✅ Isso evita bloquear o pool de conexões do banco enquanto aguarda resposta da rede

3. **Resiliência**
   - Se um servidor Mikrotik falhar, o sistema continua consultando os demais
   - Em caso de falha total, o sistema retorna zeros (degradação graciosa)
   - Logs detalhados para troubleshooting

## 📈 Métricas do Dashboard

### Dados do Banco (ACID)
- `totalServers` - Total de servidores Mikrotik cadastrados
- `activeServers` - Servidores ativos no sistema
- `totalUsers` - Total de usuários PPPoE cadastrados
- `activeUsers` - Usuários PPPoE ativos (habilitados para conexão)
- `totalProfiles` - Total de perfis PPPoE

### Dados em Tempo Real (Mikrotik)
- `onlineConnections` - **Conexões ativas neste momento** (consultado via Mikrotik API `/ppp/active/print`)
- `offlineConnections` - Usuários ativos mas desconectados (calculado: `activeUsers - onlineConnections`)
- `pendingConnections` - Usuários inativos aguardando ativação (calculado: `totalUsers - activeUsers`)
- `totalConnections` - Soma total: `online + offline + pending`

## 🔄 Fluxo de Execução

```
┌─────────────────────────────────────────────────────────────┐
│ GET /api/dashboard/stats                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ DashboardService.getStats()                                 │
│ @Transactional(readOnly = true)                            │
└─────────────────────────────────────────────────────────────┘
                           ↓
        ┌──────────────────┴──────────────────┐
        ↓                                      ↓
┌──────────────────┐              ┌────────────────────────┐
│ FASE 1:          │              │ FASE 2:                │
│ Consulta Banco   │              │ Consulta Mikrotik      │
│ (dentro da TX)   │              │ (fora da TX)           │
└──────────────────┘              └────────────────────────┘
        ↓                                      ↓
  - totalServers                    fetchRealTimeConnectionStats()
  - activeServers                            ↓
  - totalUsers                   ┌───────────────────────┐
  - activeUsers                  │ Para cada servidor:   │
  - totalProfiles                │ listActivePppoe...()  │
                                 │ (Mikrotik API)        │
                                 └───────────────────────┘
                                             ↓
                                    - onlineConnections
                                    - offlineConnections
                                    - pendingConnections
```

## 🛡️ Segurança e Multi-Tenancy

- Todas as consultas respeitam o `CompanyContextHolder.getCompanyId()`
- Apenas servidores da empresa atual são consultados
- Permissões verificadas via `@RequireModuleAccess(module = DASHBOARD, action = VIEW)`

## 🔧 Implementação

### Service Layer

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final MikrotikServerRepository mikrotikServerRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final MikrotikApiService mikrotikApiService;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        Long companyId = CompanyContextHolder.getCompanyId();
        
        // Dados do banco (dentro da transação)
        Long totalServers = mikrotikServerRepository.countByCompanyId(companyId);
        // ...
        
        // Dados do Mikrotik (fora da transação)
        ConnectionStats stats = fetchRealTimeConnectionStats(companyId);
        
        return DashboardStatsDTO.builder()
                .onlineConnections(stats.online)
                .offlineConnections(stats.offline)
                .pendingConnections(stats.pending)
                .build();
    }
}
```

### DTO Response

```json
{
  "totalServers": 5,
  "activeServers": 4,
  "totalUsers": 1247,
  "activeUsers": 1100,
  "totalProfiles": 15,
  "onlineConnections": 892,
  "offlineConnections": 245,
  "pendingConnections": 110,
  "totalConnections": 1247
}
```

## 📊 Cálculos

### Online Connections
```
onlineConnections = Σ(mikrotikServer.listActivePppoeConnections().size())
```
Soma de todas as conexões ativas retornadas por `/ppp/active/print` de cada servidor.

### Offline Connections
```
offlineConnections = max(0, activeUsers - onlineConnections)
```
Usuários que estão habilitados mas não conectados no momento.

### Pending Connections
```
pendingConnections = totalUsers - activeUsers
```
Usuários cadastrados mas inativos (aguardando ativação, pagamento, etc).

## ⚠️ Considerações de Performance

### Latência
- Consulta ao banco: ~10-50ms
- Consulta ao Mikrotik: ~100-500ms por servidor
- Total: Depende do número de servidores ativos

### Cache (Futuro)
Para sistemas com muitos servidores, considerar:
```java
@Cacheable(value = "dashboard-stats", ttl = "30s")
public DashboardStatsDTO getStats() { ... }
```

### Timeout
O `MikrotikApiService` já tem timeout configurado para evitar travamentos.

## 🧪 Testing

### Teste Manual
```bash
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/dashboard/stats
```

### Teste de Resiliência
1. Desconecte um servidor Mikrotik
2. Verifique que o sistema continua funcionando
3. Observe os logs de erro para o servidor falho

## 📝 Logs

### Nível INFO
```
Fetching dashboard statistics
Estatísticas de conexão em tempo real: Online=892, Offline=245, Pending=110, Total=1247
```

### Nível DEBUG
```
Consultando conexões ativas do servidor: Servidor Principal
Servidor Servidor Principal tem 892 conexões ativas
```

### Nível ERROR
```
Erro ao consultar servidor MK-Filial-2 (192.168.2.1): Connection timeout
```

## 🚀 Próximos Passos

1. **Cache Redis**: Cachear resultados por 30-60 segundos
2. **Métricas**: Exportar para Prometheus/Grafana
3. **Alertas**: Notificar quando utilização > 90%
4. **Dashboard por Servidor**: Detalhar conexões por cada servidor
5. **Histórico**: Armazenar snapshots para análise temporal

## 📚 Referências

- [Mikrotik API Documentation](https://wiki.mikrotik.com/wiki/Manual:API)
- [Spring Transaction Management](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#transaction)
- [CAP Theorem](https://en.wikipedia.org/wiki/CAP_theorem)

