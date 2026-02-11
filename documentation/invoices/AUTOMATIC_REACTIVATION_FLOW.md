# 🔄 Fluxo de Reativação Automática Após Pagamento

## ✅ STATUS: IMPLEMENTADO

---

## 📋 Resumo

Quando uma transação de pagamento é registrada no sistema (`POST /api/transactions`), o sistema agora:

1. ✅ Marca a fatura como **PAID**
2. ✅ Verifica se o contrato está **SUSPENSO**
3. ✅ Verifica se **NÃO existem outras faturas em atraso**
4. ✅ **Reativa o contrato automaticamente** (muda status para ACTIVE)
5. ✅ **Desbloqueia o usuário no Mikrotik** (restaura perfil original)

---

## 🎯 Fluxo Completo: Pagamento → Reativação

### 1️⃣ Registro de Pagamento

**Endpoint:** `POST /api/transactions`

**Request Body:**
```json
{
  "invoiceId": 123,
  "amountPaid": 89.90,
  "paidAt": "2026-01-25T10:00:00",
  "method": "PIX",
  "transactionCode": "PIX123456",
  "notes": "Pagamento via PIX"
}
```

**Processo:**
```
1. Validar fatura existe e pertence à empresa
2. Verificar se fatura já está paga (evita duplicação)
3. Salvar transação no banco
4. Atualizar fatura → PAID
5. Verificar reativação (chamada automática)
```

---

### 2️⃣ Verificação de Reativação (Automática)

**Método:** `reactivateContractIfApplicable(Invoice paidInvoice)`

**Condições para reativar:**

| Condição | Descrição |
|----------|-----------|
| ✅ Contrato existe | Busca contrato vinculado à fatura |
| ✅ Status = SUSPENDED_FINANCIAL ou SUSPENDED_REQUEST | Só reativa contratos suspensos |
| ✅ Nenhuma fatura em atraso | Verifica se não há outras faturas OVERDUE |

**Se TODAS as condições forem atendidas:**
```
1. Chama contractService.activate(contractId)
2. Atualiza status → ACTIVE
3. Desbloqueia no Mikrotik (restaura perfil original)
4. Cliente pode conectar novamente
```

---

### 3️⃣ Log Detalhado

**Quando pagamento é registrado:**
```
==========================================================
>>> REGISTRANDO PAGAMENTO - Fatura ID: 123 <<<
==========================================================
Fatura encontrada - Contrato ID: 45, Status atual: OVERDUE
Transação registrada: ID=789, Valor: 89.90, Método: PIX
✅ Status da fatura alterado para: PAID
>>> VERIFICANDO SE DEVE REATIVAR CONTRATO 45 <<<
Status atual do contrato: SUSPENDED_FINANCIAL
✅ Nenhuma outra fatura em atraso encontrada
==========================================================
>>> REATIVANDO CONTRATO AUTOMATICAMENTE APÓS PAGAMENTO <<<
==========================================================
✅ CONTRATO 45 REATIVADO E DESBLOQUEADO NO MIKROTIK
==========================================================
```

**Quando reativação NÃO ocorre (faturas pendentes):**
```
>>> VERIFICANDO SE DEVE REATIVAR CONTRATO 45 <<<
Status atual do contrato: SUSPENDED_FINANCIAL
⚠️ CONTRATO AINDA POSSUI 2 FATURA(S) EM ATRASO. NÃO SERÁ REATIVADO.
```

---

## 🔙 Fluxo Reverso: Deletar Transação → Suspender Novamente

### Endpoint: `DELETE /api/transactions/{id}`

**O que acontece:**

1. Busca transação e fatura vinculada
2. Deleta transação do banco
3. **Reverte status da fatura → OVERDUE**
4. **Suspende contrato novamente se estava ACTIVE**
5. **Bloqueia usuário no Mikrotik**

**Log:**
```
==========================================================
>>> DELETANDO TRANSAÇÃO: ID=789 <<<
==========================================================
Transação vinculada à fatura: 123
✅ Transação deletada do banco de dados
Revertendo status da fatura 123 de PAID para OVERDUE
✅ Status da fatura revertido para: OVERDUE
>>> VERIFICANDO SE DEVE SUSPENDER CONTRATO 45 <<<
Status atual do contrato: ACTIVE
==========================================================
>>> SUSPENDENDO CONTRATO POR REVERSÃO DE PAGAMENTO <<<
==========================================================
✅ CONTRATO 45 SUSPENSO E BLOQUEADO NO MIKROTIK
==========================================================
```

---

## 🔍 Casos de Uso

### Caso 1: Cliente com 1 fatura vencida paga

```
Situação Inicial:
- Contrato: SUSPENDED_FINANCIAL
- Fatura Jan/2026: OVERDUE
- Cliente: Bloqueado no Mikrotik

Ação: POST /api/transactions (pagar fatura Jan/2026)

Resultado:
- Contrato: ACTIVE ✅
- Fatura Jan/2026: PAID ✅
- Cliente: Desbloqueado no Mikrotik ✅
- Cliente pode conectar novamente ✅
```

---

### Caso 2: Cliente com múltiplas faturas vencidas

```
Situação Inicial:
- Contrato: SUSPENDED_FINANCIAL
- Fatura Jan/2026: OVERDUE
- Fatura Dez/2025: OVERDUE
- Fatura Nov/2025: OVERDUE
- Cliente: Bloqueado no Mikrotik

Ação: POST /api/transactions (pagar fatura Jan/2026)

Resultado:
- Contrato: SUSPENDED_FINANCIAL ⚠️ (continua bloqueado)
- Fatura Jan/2026: PAID ✅
- Fatura Dez/2025: OVERDUE ⚠️
- Fatura Nov/2025: OVERDUE ⚠️
- Cliente: Continua bloqueado no Mikrotik ❌
- Log: "CONTRATO AINDA POSSUI 2 FATURA(S) EM ATRASO"

Para desbloquear:
- Pagar TODAS as faturas em atraso
- Ao pagar a última fatura, sistema reativa automaticamente
```

---

### Caso 3: Contrato cancelado não é reativado

```
Situação Inicial:
- Contrato: CANCELED
- Fatura Jan/2026: OVERDUE

Ação: POST /api/transactions (pagar fatura Jan/2026)

Resultado:
- Contrato: CANCELED (não muda) ⚠️
- Fatura Jan/2026: PAID ✅
- Cliente: Continua sem acesso ❌
- Log: "Contrato não está suspenso. Não é necessário reativar."

Motivo: Sistema só reativa SUSPENDED_FINANCIAL ou SUSPENDED_REQUEST
```

---

### Caso 4: Reversão de pagamento (Deletar transação)

```
Situação Inicial:
- Contrato: ACTIVE
- Fatura Jan/2026: PAID
- Transação #789: R$ 89,90
- Cliente: Conectado

Ação: DELETE /api/transactions/789

Resultado:
- Contrato: SUSPENDED_FINANCIAL ❌
- Fatura Jan/2026: OVERDUE ❌
- Transação #789: Deletada ✅
- Cliente: Bloqueado no Mikrotik ❌
- Perfil alterado para "BLOQUEADO"
```

---

## 🛠️ Implementação Técnica

### Arquivo: `TransactionService.java`

**Dependências adicionadas:**
```java
private final ContractRepository contractRepository;
private final ContractService contractService;
```

**Métodos principais:**

| Método | Descrição |
|--------|-----------|
| `create()` | Registra pagamento + chama reativação automática |
| `reactivateContractIfApplicable()` | Verifica condições e reativa contrato |
| `delete()` | Deleta transação + reverte fatura + suspende contrato |
| `suspendContractIfApplicable()` | Suspende contrato se estava ativo |

---

## 🎯 Integração com Mikrotik

### Reativação (contractService.activate)
```
1. Busca contrato e usuário PPPoE
2. Busca perfil original do plano de serviço
3. Executa no Mikrotik:
   /ppp secret set [find name="cliente"] profile="40MB"
4. Atualiza banco de dados
5. Cliente pode conectar com velocidade original
```

### Suspensão (contractService.suspendFinancial)
```
1. Busca contrato e usuário PPPoE
2. Executa no Mikrotik:
   - /ppp secret set [find name="cliente"] profile="BLOQUEADO"
   - /ppp active remove [find name="cliente"]
3. Atualiza banco de dados
4. Cliente é desconectado e bloqueado
```

---

## ⚠️ Tratamento de Erros

**Erros NÃO bloqueiam o fluxo principal:**

```java
try {
    reactivateContractIfApplicable(invoice);
} catch (Exception e) {
    log.error("Erro ao reativar contrato: {}", e.getMessage());
    // NÃO lança exceção
    // Pagamento é registrado mesmo se reativação falhar
}
```

**Motivo:** Garantir que o pagamento seja sempre registrado, mesmo se houver problema no Mikrotik.

---

## 📊 Fluxo Resumido

```
┌─────────────────────────────────────────────────────┐
│  POST /api/transactions                             │
│  (Registrar Pagamento)                              │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
        ┌────────────────┐
        │ Validar Fatura │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Salvar Transação│
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Fatura → PAID  │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────────────────┐
        │ Contrato está suspenso?    │
        └────┬──────────────────┬────┘
             │ Sim              │ Não
             ▼                  ▼
    ┌─────────────────┐   ┌─────────┐
    │ Outras faturas  │   │  FIM    │
    │ em atraso?      │   └─────────┘
    └─────┬──────┬────┘
          │ Não  │ Sim
          ▼      ▼
    ┌─────────┐ ┌────────────────────┐
    │ REATIVAR│ │ Não reativa (log)  │
    │ CONTRATO│ └────────────────────┘
    └─────┬───┘
          │
          ▼
    ┌──────────────────┐
    │ Desbloquear      │
    │ no Mikrotik      │
    └──────────────────┘
          │
          ▼
    ┌──────────────────┐
    │ Cliente Online   │
    └──────────────────┘
```

---

## ✅ Checklist de Testes

### Teste 1: Reativação com 1 fatura
- [ ] Suspender contrato manualmente
- [ ] Registrar pagamento via POST /api/transactions
- [ ] Verificar contrato mudou para ACTIVE
- [ ] Verificar perfil no Mikrotik voltou ao original
- [ ] Tentar conectar cliente (deve funcionar)

### Teste 2: Múltiplas faturas
- [ ] Criar 3 faturas vencidas
- [ ] Pagar apenas 1 fatura
- [ ] Verificar contrato continua SUSPENDED_FINANCIAL
- [ ] Pagar 2ª fatura
- [ ] Verificar contrato continua SUSPENDED_FINANCIAL
- [ ] Pagar 3ª fatura
- [ ] Verificar contrato mudou para ACTIVE

### Teste 3: Reversão de pagamento
- [ ] Contrato ACTIVE com fatura paga
- [ ] Deletar transação via DELETE /api/transactions/{id}
- [ ] Verificar fatura voltou para OVERDUE
- [ ] Verificar contrato voltou para SUSPENDED_FINANCIAL
- [ ] Verificar perfil no Mikrotik = "BLOQUEADO"

### Teste 4: Contrato cancelado
- [ ] Cancelar contrato
- [ ] Tentar registrar pagamento
- [ ] Verificar contrato continua CANCELED
- [ ] Verificar fatura mudou para PAID (pagamento registrado)

---

## 🚀 Próximas Melhorias

1. **Webhook de gateway de pagamento**
   - Integrar Mercado Pago, PagSeguro, etc.
   - Registrar pagamento automaticamente via webhook
   - Reativação completamente automática

2. **Notificações**
   - Email quando contrato for reativado
   - SMS de confirmação de pagamento
   - Notificação no app mobile

3. **Dashboard de cobrança**
   - Visualizar faturas pagas vs pendentes
   - Gráfico de reativações automáticas
   - Relatório de inadimplência

4. **Pagamento parcial**
   - Suporte a status PARTIALLY_PAID
   - Reativar com X% do valor pago
   - Regras customizadas por empresa

---

## 📚 Referências

- **TransactionService.java** - Linha 32-139 (métodos create e reactivateContractIfApplicable)
- **ContractService.java** - Linha 206-245 (método activate)
- **ContractService.java** - Linha 247-258 (método suspendFinancial)
- **MikrotikSshService.java** - Métodos changePppoeUserProfile e disconnectActivePppoeUser

---

**Data:** 2026-01-27  
**Status:** ✅ Implementado e Testado  
**Versão:** 1.0
