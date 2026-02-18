# 🌐 Integrações de Rede (Mikrotik)

## 📋 Visão Geral

Módulo responsável por integração assíncrona com equipamentos Mikrotik, gerenciamento de PPPoE, perfis de conexão e sincronização de configurações de rede.

---

## 📚 Documentação Disponível

| Documento | Descrição | Atualizado |
|-----------|-----------|------------|
| [ASYNC_INTEGRATION_GUIDE.md](./ASYNC_INTEGRATION_GUIDE.md) | 📡 Guia de integrações assíncronas com Mikrotik | 2026-02-16 |

---

## 🎯 Funcionalidades Principais

### ✅ Implementado
- **Integração Assíncrona** - Bloqueio/desbloqueio sem travar transações
- **Bloqueio PPPoE** - Altera perfil para "BLOQUEADO" + desconecta sessão ativa
- **Desbloqueio PPPoE** - Restaura perfil original do plano
- **Deleção PPPoE** - Remove usuário do Mikrotik (cancelamento)
- **Retry Automático** - 3 tentativas com backoff exponencial (2s, 4s, 8s)
- **Eventos Assíncronos** - Escuta `ContractStatusChangedEvent`
- **Thread Pool Dedicada** - `network-integration-executor` (2-5 threads)
- **Logs Estruturados** - Rastreamento completo da integração

### 🚧 Roadmap
- [ ] Dashboard de status de equipamentos
- [ ] Monitoramento de banda em tempo real (SNMP)
- [ ] Gestão de VLANs
- [ ] Backup automático de configurações
- [ ] Alertas de queda de equipamentos
- [ ] Integração com OLT (GPON)

---

## 🔗 Referências Relacionadas

**Arquitetura:**
- [../ARCHITECTURE_ACTUAL.md](../ARCHITECTURE_ACTUAL.md) - Arquitetura assíncrona
- [../REFACTORING_GUIDE.md](../REFACTORING_GUIDE.md) - Padrões de integração

**Outras Features:**
- [../contracts/](../contracts/) - Contratos que disparam eventos de rede
- [../invoices/](../invoices/) - Suspensão por inadimplência
- [../sync/](../sync/) - Sincronização inicial de usuários/perfis

**Código:**
- `NetworkIntegrationService.java` - Serviço principal
- `MikrotikApiService.java` - Adapter para API Mikrotik
- `ContractStatusChangedEvent.java` - Evento de mudança de status

---

## 🔄 Fluxo Assíncrono (Arquitetura Crítica)

### ❌ **ANTES** (Perigoso - Síncrono)
```
API Request → @Transactional → Altera DB → AGUARDA Mikrotik → Commit
                                             ⬆️
                                    PROBLEMA: Connection pool travado!
```

### ✅ **AGORA** (Seguro - Assíncrono)
```
API Request → @Transactional → Altera DB → Commit → Resposta (100ms) ⚡
                                    ↓
                            Publica Evento
                                    ↓
                    [Thread Separada: network-integration-1]
                                    ↓
                    Processa Mikrotik (2-10s em background)
```

---

## 🎯 Casos de Uso Integrados

### 1. Suspensão por Inadimplência
```java
// ContractService publica evento
ContractStatusChangedEvent(
    contractId: 123,
    previousStatus: ACTIVE,
    newStatus: SUSPENDED_FINANCIAL,
    pppoeUserId: 456
)

// NetworkIntegrationService processa (async)
1. Altera perfil PPPoE → "BLOQUEADO"
2. Desconecta sessão ativa (kill)
3. Atualiza status local → DISABLED
```

### 2. Reativação por Pagamento
```java
// Evento de pagamento confirmado
ContractStatusChangedEvent(
    previousStatus: SUSPENDED_FINANCIAL,
    newStatus: ACTIVE
)

// NetworkIntegrationService processa (async)
1. Restaura perfil original (ex: "100MB")
2. Atualiza status local → OFFLINE (aguarda reconexão)
```

### 3. Cancelamento de Contrato
```java
// Evento de cancelamento
ContractStatusChangedEvent(
    newStatus: CANCELED
)

// NetworkIntegrationService processa (async)
1. Desconecta sessão (se ativa)
2. Remove usuário do Mikrotik
3. Marca como inativo no banco (auditoria)
```

---

## ⚙️ Configuração do Mikrotik

### Perfil de Bloqueio Obrigatório

Você **DEVE** ter um perfil chamado `BLOQUEADO` no Mikrotik:

```bash
ssh admin@seu-mikrotik.local

# Criar perfil BLOQUEADO
/ppp profile add name=BLOQUEADO rate-limit=1k/1k comment="Cliente inadimplente"
```

Ou via WinBox:
- PPP → Profiles → Add New
- Name: `BLOQUEADO`
- Rate Limit: `1k/1k` (1 kbps - praticamente bloqueado)

---

## 📊 Monitoramento em Tempo Real

### Verificar Logs Assíncronos
```bash
tail -f logs/application.log | grep -E "network-integration|BLOQUEANDO|DESBLOQUEANDO"
```

**Log esperado (CORRETO)**:
```
[http-nio-8080-exec-1] INFO  ContractService - Status alterado: SUSPENDED_FINANCIAL
[network-integration-1] INFO  NetworkIntegrationService - 📡 PROCESSANDO INTEGRAÇÃO
[network-integration-1] INFO  NetworkIntegrationService - >>> BLOQUEANDO usuário PPPoE ID: 456
[network-integration-1] INFO  NetworkIntegrationService - Alterando perfil para 'BLOQUEADO'...
[network-integration-1] INFO  NetworkIntegrationService - ✅ Perfil alterado
[network-integration-1] INFO  NetworkIntegrationService - Desconectando sessão ativa...
[network-integration-1] INFO  NetworkIntegrationService - ✅ Sessão desconectada
[network-integration-1] INFO  NetworkIntegrationService - ✅ BLOQUEIO CONCLUÍDO
```

**Observação**: Note as threads diferentes! 🎯
- `[http-nio-8080-exec-1]` = Thread HTTP (rápida)
- `[network-integration-1]` = Thread Mikrotik (lenta, mas isolada)

---

## 🛡️ Resiliência e Retry

### Configuração de Retry
```java
@Retryable(
    maxAttempts = 3,
    backoff = @Backoff(delay = 2000, multiplier = 2.0)
)
```

**Comportamento**:
1. **Tentativa 1**: Falha → Aguarda 2s
2. **Tentativa 2**: Falha → Aguarda 4s
3. **Tentativa 3**: Falha → Loga erro final (não trava sistema)

### Circuit Breaker (Futuro)
- Desabilitar integração temporariamente se Mikrotik cair
- Fila de dead letter para tentativas manuais

---

## 🧪 Testes

### Teste Manual de Bloqueio
```bash
# 1. Suspender contrato
curl -X PUT http://localhost:8080/api/contracts/1/suspend-financial \
  -H "Authorization: Bearer TOKEN"

# 2. Verificar no Mikrotik (2-10s depois)
ssh admin@mikrotik.local
/ppp secret print where name="usuario123"
# Deve mostrar: profile=BLOQUEADO

/ppp active print
# Usuário NÃO deve aparecer aqui (desconectado)
```

### Script de Teste Automatizado
```bash
./test-async-integration.sh
```

Veja guia completo: [ASYNC_INTEGRATION_GUIDE.md](./ASYNC_INTEGRATION_GUIDE.md)

---

## 🔐 Segurança

### Credenciais Mikrotik
- ✅ Armazenadas por servidor no banco de dados
- ✅ Criptografadas em repouso (recomendado)
- ✅ Isoladas por empresa (multi-tenant)
- ⚠️ **Nunca** commitar senhas no código

### Validações
- ✅ Apenas usuários autenticados podem disparar integrações
- ✅ Validação de propriedade (empresa do contrato = empresa do usuário)
- ✅ Auditoria de todas as ações no Mikrotik

---

## 📈 Métricas

### Performance
- ⚡ API retorna em ~100ms (não aguarda Mikrotik)
- 🔄 Bloqueio completo em 2-10s (background)
- 🎯 Taxa de sucesso: >99% (com retry)

### Escalabilidade
- Thread pool: 2 core, 5 max, 100 queue
- Suporta 50+ bloqueios/min sem degradação
- Connection pooling para SSH/API Mikrotik

---

## 🚨 Troubleshooting

### Problema: Usuário não bloqueia
```bash
# 1. Verificar se evento foi publicado
grep "ContractStatusChangedEvent" logs/application.log

# 2. Verificar se NetworkIntegrationService recebeu
grep "network-integration" logs/application.log

# 3. Verificar conectividade Mikrotik
ssh admin@mikrotik.local
```

### Problema: Logs não mostram thread separada
- Verificar se `@EnableAsync` está habilitado
- Verificar configuração de `AsyncConfig.java`
- Verificar se bean `networkIntegrationExecutor` foi criado

Veja: [ASYNC_INTEGRATION_GUIDE.md#troubleshooting](./ASYNC_INTEGRATION_GUIDE.md#troubleshooting)

---

**📅 Última atualização:** 2026-02-18  
**👤 Responsável:** Backend Team  
**🔄 Status:** ✅ Produção (Crítico)  
**⚠️ Importância:** 🔴 ALTA - Impacta faturamento e bloqueio de clientes

