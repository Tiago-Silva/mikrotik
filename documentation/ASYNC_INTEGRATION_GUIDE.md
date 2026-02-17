# 📡 Guia de Integrações Assíncronas com Mikrotik

**Data de implementação**: 2026-02-16  
**Versão**: 1.0  
**Status**: ✅ Ativo

---

## 🎯 O Que Mudou?

### ❌ **ANTES** (Integração Síncrona - Perigoso)
```
Cliente chama API → Status muda no banco → AGUARDA Mikrotik responder → Retorna resposta
                                          ⬆️
                                    PROBLEMA: Se Mikrotik demorar 30s,
                                    conexão do banco fica travada!
```

### ✅ **AGORA** (Integração Assíncrona - Seguro)
```
Cliente chama API → Status muda no banco → Retorna resposta IMEDIATAMENTE
                           ↓
                    Publica evento
                           ↓
                    (Thread separada processa Mikrotik em background)
```

---

## 🔄 Como Funciona?

### **Exemplo: Suspender Contrato por Inadimplência**

```bash
POST /api/contracts/123/suspend-financial
```

**Fluxo Interno**:

1. **ContractService.suspendFinancial(123)** - Thread: `[http-nio-8080-exec-1]`
   - Altera `contract.status = SUSPENDED_FINANCIAL` no banco
   - Commit da transação ✅
   - Publica `ContractStatusChangedEvent`
   - **Retorna HTTP 200 IMEDIATAMENTE** ⚡

2. **NetworkIntegrationService.handleContractStatusChange()** - Thread: `[network-integration-1]`
   - Recebe evento em thread separada
   - Chama Mikrotik API: `changePppoeUserProfile("BLOQUEADO")`
   - Desconecta sessão ativa
   - Atualiza status do PppoeUser
   - **Retry automático**: Se falhar, tenta 3x (2s, 4s, 8s de intervalo)

**Tempo total**:
- API retorna: ~100ms ⚡
- Bloqueio no Mikrotik: 2-10 segundos (em background)

---

## 📊 Monitoramento

### **Como saber se está funcionando?**

#### 1. **Verificar Logs de Thread Separada**

```bash
tail -f logs/application.log | grep "network-integration"
```

**Exemplo de log correto**:
```
2026-02-16 14:30:15.123 [http-nio-8080-exec-1] INFO  ContractService - Status alterado para: SUSPENDED_FINANCIAL (bloqueio será processado assíncronamente)
2026-02-16 14:30:15.125 [network-integration-1] INFO  NetworkIntegrationService - 📡 PROCESSANDO INTEGRAÇÃO MIKROTIK - Contrato ID: 123
2026-02-16 14:30:15.126 [network-integration-1] INFO  NetworkIntegrationService - >>> BLOQUEANDO usuário PPPoE ID: 456
2026-02-16 14:30:17.890 [network-integration-1] INFO  NetworkIntegrationService - ✅ BLOQUEIO CONCLUÍDO
```

**Observação**: Note as threads diferentes! 🎯
- `[http-nio-8080-exec-1]` = Thread da requisição HTTP (rápida)
- `[network-integration-1]` = Thread dedicada para Mikrotik (lenta, mas não trava o sistema)

---

#### 2. **Verificar Status no Banco vs Mikrotik**

```sql
-- Status no banco (deve mudar IMEDIATAMENTE)
SELECT id, status, pppoe_user_id 
FROM contracts 
WHERE id = 123;

-- Status: SUSPENDED_FINANCIAL ✅
```

```bash
# Status no Mikrotik (pode levar 2-10 segundos)
ssh admin@mikrotik.local
/ppp secret print where name="cliente123"
# Deve mostrar profile=BLOQUEADO ✅
```

---

#### 3. **Verificar Métricas de Thread Pool**

As threads são limitadas:
- **Core Pool**: 2 threads
- **Max Pool**: 5 threads
- **Queue**: 100 jobs pendentes

**Logs de inicialização**:
```
✅ NetworkIntegrationExecutor configurado: core=2, max=5, queue=100
```

**Alerta crítico** (se aparecer):
```
❌ CRÍTICO: Fila de integrações de rede CHEIA. Job rejeitado
```
→ Significa que há mais de 100 suspensões/ativações pendentes simultaneamente!  
→ Ação: Aumentar `maxPoolSize` ou `queueCapacity` no `AsyncConfig.java`

---

## ⚠️ Cenários de Falha e Resolução

### **Cenário 1: Mikrotik Está Offline**

**Sintoma**:
```bash
❌ ERRO ao processar integração Mikrotik para contrato 123
me.legrange.mikrotik.ApiConnectionException: Cannot connect to 192.168.1.1:8728
```

**Consequência**:
- ✅ Status no banco: `SUSPENDED_FINANCIAL` (correto)
- ❌ Status no Mikrotik: Cliente ainda consegue conectar! (inconsistência)

**O que o sistema faz automaticamente**:
1. Tenta 3x com retry (2s, 4s, 8s)
2. Se falhar as 3x, loga erro mas **não trava**

**O que VOCÊ deve fazer**:
1. Verificar conectividade com Mikrotik
2. Após Mikrotik voltar, **reprocessar manualmente**:

```bash
# Opção A: Via API (quando implementarmos endpoint de reconciliação)
POST /api/contracts/123/reconcile-mikrotik

# Opção B: Suspender e reativar (força nova tentativa)
POST /api/contracts/123/activate
POST /api/contracts/123/suspend-financial
```

---

### **Cenário 2: Fila de Threads Cheia**

**Sintoma**:
```bash
❌ CRÍTICO: Fila de integrações de rede CHEIA. Job rejeitado
```

**Causa**:
- Suspensão automática em lote (ex: 150 contratos inadimplentes processados de uma vez)

**Solução Imediata**:
- Aguardar fila esvaziar (threads vão processar aos poucos)

**Solução Permanente**:
Editar `AsyncConfig.java`:
```java
executor.setCorePoolSize(5);  // Era 2
executor.setMaxPoolSize(10);  // Era 5
executor.setQueueCapacity(200); // Era 100
```

---

### **Cenário 3: Evento Não Foi Capturado**

**Sintoma**:
- Status muda no banco
- Nenhum log de `NetworkIntegrationService`
- Cliente continua navegando

**Possíveis causas**:
1. `@EnableAsync` não está habilitado
2. `@EnableRetry` não está habilitado
3. Bean `NetworkIntegrationService` não foi criado

**Verificar**:
```bash
# Logs de inicialização devem conter:
grep "NetworkIntegrationExecutor configurado" logs/application.log
```

**Solução**:
- Garantir que `MikrotikApplication.java` tem `@EnableAsync` e `@EnableRetry`

---

## 🧪 Testes Manuais

### **Teste 1: Suspensão Básica**

```bash
# 1. Criar contrato ativo
POST /api/contracts
{
  "customerId": 1,
  "servicePlanId": 1,
  "billingDay": 10,
  "amount": 99.90
}

# 2. Ativar contrato (cria credencial PPPoE)
POST /api/contracts/{id}/activate

# 3. Suspender contrato
POST /api/contracts/{id}/suspend-financial

# 4. Verificar logs
tail -f logs/application.log | grep "network-integration"

# 5. Verificar Mikrotik (após 5-10 segundos)
ssh admin@mikrotik /ppp secret print
```

---

### **Teste 2: Cenário de Retry**

```bash
# 1. Parar servidor Mikrotik (desligar ou bloquear porta 8728)
sudo iptables -A OUTPUT -p tcp --dport 8728 -j DROP

# 2. Suspender contrato
POST /api/contracts/{id}/suspend-financial

# 3. Observar logs (deve tentar 3x)
# Esperado:
# - Tentativa 1: falha imediata
# - Aguarda 2s
# - Tentativa 2: falha
# - Aguarda 4s
# - Tentativa 3: falha
# - Loga erro final

# 4. Restaurar conectividade
sudo iptables -D OUTPUT -p tcp --dport 8728 -j DROP

# 5. Reprocessar manualmente (ver Cenário 1)
```

---

## 📋 Checklist de Validação em Produção

Antes de confiar 100% no sistema:

```markdown
- [ ] 1. Logs mostram threads separadas (`[network-integration-X]`)
- [ ] 2. API retorna rápido (< 500ms) mesmo com Mikrotik lento
- [ ] 3. Suspensão em lote (10+ contratos) não trava sistema
- [ ] 4. Retry funciona (testar com Mikrotik offline)
- [ ] 5. Erro de fila cheia NÃO aparece em operação normal
- [ ] 6. Após 1 semana, conferir inconsistências (banco vs Mikrotik)
```

---

## 🔧 Configurações Avançadas

### **Ajustar Timeout de Retry**

Editar `NetworkIntegrationService.java`:
```java
@Retryable(
    maxAttempts = 5,           // Era 3 (mais tentativas)
    backoff = @Backoff(
        delay = 5000,          // Era 2000 (aguardar mais entre tentativas)
        multiplier = 2.0       // Padrão (dobrar tempo a cada tentativa)
    )
)
```

### **Aumentar Capacidade de Thread Pool**

Editar `AsyncConfig.java`:
```java
executor.setCorePoolSize(10);   // Para ISPs maiores
executor.setMaxPoolSize(20);
executor.setQueueCapacity(500);
```

---

## 📞 Suporte

**Em caso de problemas**:
1. Verificar logs: `logs/application.log`
2. Buscar por: `"❌ ERRO"` ou `"CRÍTICO"`
3. Documentar: contractId, timestamp, mensagem de erro
4. Reprocessar manualmente (ver Cenário 1)

**Contato**: (deixar em branco - preencher quando tiver equipe)

---

**Última atualização**: 2026-02-16  
**Autor**: Arquiteto Backend (IA)

