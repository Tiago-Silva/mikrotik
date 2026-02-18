# 🔄 Sincronização com Mikrotik

## 📋 Visão Geral

Módulo responsável por importar configurações existentes do Mikrotik (usuários PPPoE e perfis) para o banco de dados da aplicação, facilitando a migração de infraestrutura legada.

---

## 📚 Documentação Disponível

| Documento | Descrição | Atualizado |
|-----------|-----------|------------|
| [SYNC_USERS.md](./SYNC_USERS.md) | 🔄 Guia de sincronização de usuários PPPoE | 2026-02-18 |
| [SYNC_PROFILES.md](./SYNC_PROFILES.md) | 🔄 Guia de sincronização de perfis PPPoE | 2026-02-18 |
| [SYNC_PROFILES_IMPLEMENTATION.md](./SYNC_PROFILES_IMPLEMENTATION.md) | 🔧 Detalhes técnicos da implementação | 2026-02-18 |
| [SYNC_FEATURE_SUMMARY.md](./SYNC_FEATURE_SUMMARY.md) | 📝 Resumo executivo das funcionalidades | 2026-02-18 |

---

## 🎯 Funcionalidades Principais

### ✅ Implementado
- **Sincronização de Perfis PPPoE** - Importa rate-limit, timeout, comentários
- **Sincronização de Usuários PPPoE** - Importa username, password, profile, service
- **Associação Automática** - Vincula usuários aos perfis corretos
- **Validação de Duplicatas** - Impede importação de usuários já existentes
- **Multi-servidor** - Suporta múltiplos Mikrotiks
- **Auditoria** - Registra origem da sincronização (Mikrotik X)
- **Rollback Parcial** - Se falhar um usuário, outros continuam

### 🚧 Roadmap
- [ ] Sincronização bidirecional (API → Mikrotik)
- [ ] Sincronização incremental (apenas novos/alterados)
- [ ] Agendamento automático de sincronização
- [ ] Comparação de diferenças (diff)
- [ ] Sincronização de IPs estáticos
- [ ] Sincronização de queues

---

## 🔗 Referências Relacionadas

**Arquitetura:**
- [../ARCHITECTURE_ACTUAL.md](../ARCHITECTURE_ACTUAL.md) - Arquitetura do sistema
- [../REFACTORING_GUIDE.md](../REFACTORING_GUIDE.md) - Padrões de código

**Outras Features:**
- [../network/](../network/) - Integração assíncrona com Mikrotik
- [../contracts/](../contracts/) - Vinculação de usuários PPPoE a contratos
- [../customers/](../customers/) - Clientes do ISP

**Código:**
- `SyncService.java` - Serviço de sincronização
- `MikrotikSshService.java` - Comunicação SSH com Mikrotik
- `PppoeUserRepository.java` - Persistência de usuários

---

## 🚀 Fluxo de Sincronização

### 1️⃣ Sincronizar Perfis (Obrigatório Primeiro)
```
API → SSH Mikrotik → /ppp profile print
                          ↓
                  Parse dos perfis
                          ↓
                  Validação/Criação
                          ↓
                  Salva no banco de dados
```

**Endpoint:**
```bash
POST /api/sync/profiles/{mikrotikServerId}
```

### 2️⃣ Sincronizar Usuários
```
API → SSH Mikrotik → /ppp secret print
                          ↓
                  Parse dos usuários
                          ↓
              Associa com perfis existentes
                          ↓
              Valida duplicatas
                          ↓
              Salva no banco de dados
```

**Endpoint:**
```bash
POST /api/sync/users/{mikrotikServerId}
```

---

## 📊 Exemplo Prático

### Cenário: Migração de Mikrotik Legado

Você já tem 500 clientes PPPoE no Mikrotik e quer gerenciar via API.

#### Passo 1: Sincronizar Perfis
```bash
curl -X POST http://localhost:8080/api/sync/profiles/1 \
  -H "Authorization: Bearer TOKEN"
```

**Resultado:**
```json
{
  "success": true,
  "message": "10 perfis sincronizados com sucesso",
  "synced": ["100MB", "200MB", "500MB", "BLOQUEADO", ...]
}
```

#### Passo 2: Sincronizar Usuários
```bash
curl -X POST http://localhost:8080/api/sync/users/1 \
  -H "Authorization: Bearer TOKEN"
```

**Resultado:**
```json
{
  "success": true,
  "message": "500 usuários sincronizados",
  "synced": 500,
  "skipped": 0,
  "errors": []
}
```

---

## ⚙️ Mapeamento de Dados

### Perfis PPPoE
| Campo Mikrotik | Campo API | Observação |
|----------------|-----------|------------|
| `name` | `name` | Nome do perfil |
| `rate-limit` | `maxBitrateDl` / `maxBitrateUl` | Parse de "10M/20M" |
| `session-timeout` | `sessionTimeout` | Em segundos |
| `comment` | `description` | Descrição opcional |

### Usuários PPPoE
| Campo Mikrotik | Campo API | Observação |
|----------------|-----------|------------|
| `name` | `username` | Identificador único |
| `password` | `password` | Senha (criptografada) |
| `profile` | `profile_id` | FK para perfil |
| `service` | `service` | Ex: "pppoe", "pptp" |
| `comment` | `comment` | Comentários |

---

## 🛡️ Validações e Segurança

### Validações Aplicadas
- ✅ Perfil deve existir antes de sincronizar usuários
- ✅ Username deve ser único por servidor
- ✅ Rate-limit deve ser parse-ável (formato: "10M/20M")
- ✅ Servidor Mikrotik deve estar acessível via SSH

### Segurança
- ✅ Apenas roles ADMIN/OPERATOR podem sincronizar
- ✅ Senhas dos usuários são mantidas (não alteradas)
- ✅ Multi-tenant: sincronização isolada por empresa
- ✅ Auditoria: registra quem sincronizou e quando

---

## 📈 Performance

### Capacidade
- 1000 perfis: ~5-10 segundos
- 5000 usuários: ~30-60 segundos
- Limitação: velocidade SSH do Mikrotik

### Otimizações Futuras
- [ ] Batch insert (reduzir queries)
- [ ] Paginação de sincronização
- [ ] Cache de perfis durante sync de usuários

---

## 🚨 Troubleshooting

### Problema: "Profile not found"
**Causa:** Usuário referencia perfil que não foi sincronizado

**Solução:**
1. Sincronizar perfis primeiro: `POST /api/sync/profiles/{serverId}`
2. Depois sincronizar usuários: `POST /api/sync/users/{serverId}`

### Problema: "Duplicate username"
**Causa:** Usuário já existe no banco

**Solução:**
- Sincronização pula duplicatas automaticamente
- Verificar logs para detalhes: `grep "SYNC" logs/application.log`

### Problema: Timeout SSH
**Causa:** Mikrotik não responde ou firewall bloqueando

**Solução:**
```bash
# Testar conectividade
ssh admin@mikrotik.local

# Verificar configuração do servidor no banco
SELECT * FROM mikrotik_servers WHERE id = 1;
```

Veja mais: [SYNC_USERS.md#troubleshooting](./SYNC_USERS.md#troubleshooting)

---

## 🧪 Testes

### Teste Completo de Sincronização
```bash
# 1. Cadastrar servidor Mikrotik
curl -X POST http://localhost:8080/api/mikrotik-servers \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "name": "Concentrador Principal",
    "ipAddress": "192.168.1.1",
    "username": "admin",
    "password": "senha123"
  }'

# 2. Sincronizar perfis
curl -X POST http://localhost:8080/api/sync/profiles/1 \
  -H "Authorization: Bearer TOKEN"

# 3. Sincronizar usuários
curl -X POST http://localhost:8080/api/sync/users/1 \
  -H "Authorization: Bearer TOKEN"

# 4. Verificar resultados
curl http://localhost:8080/api/pppoe-users \
  -H "Authorization: Bearer TOKEN"
```

---

## 📝 Boas Práticas

### ✅ FAZER
- Sincronizar perfis **ANTES** de sincronizar usuários
- Fazer backup do Mikrotik antes de sincronização reversa (futuro)
- Testar em ambiente de homologação primeiro
- Validar dados sincronizados antes de usar em produção

### ❌ NÃO FAZER
- Não sincronizar durante horário de pico
- Não alterar dados no Mikrotik durante sincronização
- Não sincronizar múltiplas vezes seguidas (criar duplicatas)

---

## 🔄 Sincronização Reversa (Futuro)

Planejamento para sincronizar mudanças da API → Mikrotik:

```
API (fonte de verdade) → Detecta diferenças → Aplica no Mikrotik
                              ↓
                        Cria/Atualiza/Remove
                              ↓
                        Valida aplicação
```

**Status:** 🚧 Em planejamento

---

**📅 Última atualização:** 2026-02-18  
**👤 Responsável:** Backend Team  
**🔄 Status:** ✅ Produção  
**📌 Nota:** Executar sincronização apenas durante migração inicial ou quando houver dessincronia

