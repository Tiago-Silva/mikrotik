# 🧾 Faturamento e Cobrança

## 📋 Visão Geral

Módulo responsável por geração automática de faturas, controle de vencimentos, suspensão por inadimplência e reativação automática após pagamento.

---

## 📚 Documentação Disponível

| Documento | Descrição | Atualizado |
|-----------|-----------|------------|
| [AUTOMATIC_REACTIVATION_FLOW.md](./AUTOMATIC_REACTIVATION_FLOW.md) | 🔄 Fluxo de reativação automática após pagamento | 2026-02-18 |
| [MANUAL_SUSPENSION_TEST.md](./MANUAL_SUSPENSION_TEST.md) | 🧪 Guia de teste manual de suspensão | 2026-02-18 |
| [TESTING_GUIDE_AUTOMATIC_SUSPENSION.md](./TESTING_GUIDE_AUTOMATIC_SUSPENSION.md) | 📘 Guia de testes de suspensão automática | 2026-02-18 |
| [TROUBLESHOOTING_SUSPENSION.md](./TROUBLESHOOTING_SUSPENSION.md) | 🚨 Solução de problemas de suspensão | 2026-02-18 |
| [test-suspension.http](./test-suspension.http) | 📝 Exemplos de requisições HTTP para testes | 2026-02-18 |

---

## 🎯 Funcionalidades Principais

### ✅ Implementado
- **Geração Automática de Faturas** - Job agendado mensal
- **Suspensão por Inadimplência** - Bloqueio automático após vencimento
- **Reativação Automática** - Desbloqueio após confirmação de pagamento
- **Cálculo de Juros e Multa** - Configurável por empresa
- **Notificações** - Alertas de vencimento próximo
- **Histórico de Faturas** - Auditoria completa
- **Multi-tenant** - Isolamento por empresa
- **Integração com Rede** - Bloqueio/desbloqueio assíncrono no Mikrotik

### 🚧 Roadmap
- [ ] Geração de boletos bancários
- [ ] Integração com gateways de pagamento (Pix, Cartão)
- [ ] Segunda via de fatura por e-mail
- [ ] Parcelamento de débitos
- [ ] Descontos progressivos para pagamento antecipado
- [ ] Dashboard de inadimplência

---

## 🔗 Referências Relacionadas

**Arquitetura:**
- [../ARCHITECTURE_ACTUAL.md](../ARCHITECTURE_ACTUAL.md) - Arquitetura do sistema
- [../network/ASYNC_INTEGRATION_GUIDE.md](../network/ASYNC_INTEGRATION_GUIDE.md) - Integração com Mikrotik

**Outras Features:**
- [../contracts/](../contracts/) - Contratos e planos
- [../financial/](../financial/) - Fluxo de caixa e lançamentos
- [../network/](../network/) - Bloqueio/desbloqueio PPPoE
- [../customers/](../customers/) - Clientes

**Código:**
- `InvoiceService.java` - Geração e gestão de faturas
- `InvoiceBillingJob.java` - Job de cobrança automática
- `TransactionService.java` - Processamento de pagamentos

---

## 🔄 Fluxo Completo: Faturamento → Suspensão → Reativação

### 1️⃣ Geração de Fatura (Dia 1 do mês)
```
Job Agendado → Busca contratos ACTIVE
                    ↓
              Gera fatura mensal
                    ↓
            Calcula valor do plano
                    ↓
            Define data de vencimento
                    ↓
              Status: PENDING
                    ↓
            Envia notificação
```

### 2️⃣ Suspensão Automática (Após Vencimento + Tolerância)
```
Job Verifica Inadimplência
          ↓
    Fatura vencida > X dias?
          ↓
Publica ContractStatusChangedEvent
          ↓
    ACTIVE → SUSPENDED_FINANCIAL
          ↓
NetworkIntegrationService (async)
          ↓
    Altera perfil → BLOQUEADO
          ↓
    Desconecta sessão ativa
          ↓
    Cliente sem internet ❌
```

### 3️⃣ Pagamento Confirmado
```
Webhook/Manual → TransactionService
                      ↓
              Registra pagamento
                      ↓
              Marca fatura: PAID
                      ↓
    Publica ContractStatusChangedEvent
                      ↓
        SUSPENDED_FINANCIAL → ACTIVE
                      ↓
    NetworkIntegrationService (async)
                      ↓
        Restaura perfil original
                      ↓
        Cliente pode reconectar ✅
```

---

## 📊 Status de Faturas

| Status | Descrição | Ação |
|--------|-----------|------|
| `PENDING` | Aguardando pagamento | Nenhuma (dentro do prazo) |
| `OVERDUE` | Vencida | Suspender após tolerância |
| `PAID` | Paga | Reativar se suspenso |
| `CANCELED` | Cancelada | Nenhuma |

---

## ⚙️ Configurações de Cobrança

### Parâmetros Configuráveis (por Empresa)
```java
// application.yml
billing:
  grace-period-days: 5        # Dias de tolerância após vencimento
  suspension-enabled: true     # Suspensão automática ativada
  late-fee-percentage: 2.0    # Multa por atraso (%)
  interest-per-day: 0.033     # Juros por dia (%)
```

### Job de Faturamento Automático
```java
@Scheduled(cron = "0 0 2 1 * ?")  // Todo dia 1 às 02:00
public void generateMonthlyInvoices() {
    // Gera faturas para todos os contratos ACTIVE
}
```

---

## 🧪 Testes

### Teste Manual de Suspensão
Veja: [MANUAL_SUSPENSION_TEST.md](./MANUAL_SUSPENSION_TEST.md)

```bash
# 1. Criar contrato com vencimento passado
# 2. Executar job de suspensão
# 3. Verificar bloqueio no Mikrotik
# 4. Registrar pagamento
# 5. Verificar desbloqueio
```

### Teste Automatizado
Veja: [TESTING_GUIDE_AUTOMATIC_SUSPENSION.md](./TESTING_GUIDE_AUTOMATIC_SUSPENSION.md)

```bash
# Executar suite de testes
./mvnw test -Dtest=InvoiceSuspensionFlowTest
```

### Exemplos HTTP
Veja: [test-suspension.http](./test-suspension.http)

---

## 🚨 Troubleshooting

### Problema: Cliente pagou mas continua bloqueado

**Causas possíveis:**
1. Pagamento não foi registrado no sistema
2. Fatura não foi marcada como PAID
3. Evento de reativação não foi disparado
4. Falha na integração com Mikrotik

**Solução:**
Veja guia completo: [TROUBLESHOOTING_SUSPENSION.md](./TROUBLESHOOTING_SUSPENSION.md)

```bash
# 1. Verificar status da fatura
curl http://localhost:8080/api/invoices/{invoiceId}

# 2. Verificar logs de integração
grep "network-integration" logs/application.log

# 3. Reativar manualmente se necessário
curl -X PUT http://localhost:8080/api/contracts/{id}/activate
```

---

## 🛡️ Regras de Negócio

### Suspensão
- ✅ Apenas contratos ACTIVE podem ser suspensos
- ✅ Suspensão respeita período de tolerância (grace period)
- ✅ Bloqueio no Mikrotik é assíncrono (não trava banco)
- ✅ Cliente recebe notificação antes da suspensão

### Reativação
- ✅ Apenas contratos SUSPENDED_FINANCIAL podem ser reativados
- ✅ Requer confirmação de pagamento (fatura PAID)
- ✅ Desbloqueio no Mikrotik é assíncrono
- ✅ Cliente recebe notificação de reativação

### Cancelamento
- ✅ Contrato cancelado não gera mais faturas
- ✅ Usuário PPPoE é removido do Mikrotik
- ✅ Faturas pendentes permanecem (cobrança)

---

## 📈 Métricas e KPIs

### Indicadores de Inadimplência
```sql
-- Taxa de inadimplência
SELECT 
    COUNT(CASE WHEN status = 'OVERDUE' THEN 1 END) * 100.0 / COUNT(*) as inadimplencia_percent
FROM invoices
WHERE due_date >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Valor total em atraso
SELECT SUM(amount) as total_overdue
FROM invoices
WHERE status = 'OVERDUE';
```

### Performance do Sistema
- ⚡ Geração de 1000 faturas: ~30-60 segundos
- 🔄 Suspensão de 100 contratos: ~5-10 minutos (assíncrono)
- ✅ Taxa de sucesso de bloqueio: >99% (com retry)

---

## 🔐 Segurança e Auditoria

### Validações
- ✅ Apenas ADMIN/FINANCIAL pode cancelar faturas
- ✅ Pagamento manual requer confirmação dupla
- ✅ Alteração de valor de fatura gera log de auditoria
- ✅ Multi-tenant: empresa só vê suas próprias faturas

### Logs de Auditoria
```java
// Registrado automaticamente
AuditLog:
- Quem registrou o pagamento
- Quando foi registrado
- Valor pago
- Método de pagamento
- IP de origem
```

---

## 💡 Boas Práticas

### ✅ FAZER
- Configurar período de tolerância adequado (5-10 dias)
- Notificar cliente antes de suspender
- Monitorar logs de integração com Mikrotik
- Fazer backup antes de executar jobs em produção

### ❌ NÃO FAZER
- Não alterar status de fatura manualmente no banco
- Não desabilitar retry de integração
- Não executar job de suspensão em horário de pico
- Não remover auditoria de pagamentos

---

## 📅 Calendário de Execução

| Dia | Hora | Job | Descrição |
|-----|------|-----|-----------|
| 1 | 02:00 | Geração de Faturas | Gera faturas mensais |
| Diário | 03:00 | Verificação de Inadimplência | Suspende contratos vencidos |
| Diário | 04:00 | Notificações | Alerta de vencimento próximo |

---

## 🔄 Fluxo de Reativação Automática

Veja detalhes completos: [AUTOMATIC_REACTIVATION_FLOW.md](./AUTOMATIC_REACTIVATION_FLOW.md)

```
Pagamento Confirmado
        ↓
TransactionService.create()
        ↓
Marca fatura como PAID
        ↓
ContractService.activate()
        ↓
Publica ContractStatusChangedEvent
        ↓
NetworkIntegrationService (async)
        ↓
Restaura perfil original
        ↓
Cliente reconecta ✅
```

---

**📅 Última atualização:** 2026-02-18  
**👤 Responsável:** Backend Team  
**🔄 Status:** ✅ Produção (Crítico)  
**⚠️ Importância:** 🔴 ALTA - Impacta faturamento e cash flow da empresa

