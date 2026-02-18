# 📝 Contratos e Planos

## 📋 Visão Geral

Módulo responsável por gestão de contratos de serviços, planos de internet, ciclos de cobrança e vínculo com clientes e usuários PPPoE.

---

## 📚 Documentação Disponível

🚧 **Em construção** - Documentação específica será adicionada em breve.

---

## 🎯 Funcionalidades Principais

### ✅ Implementado
- **CRUD de Contratos** - Criar, listar, atualizar, cancelar
- **Planos de Serviço** - Definição de velocidade, preço, ciclo
- **Status de Contratos** - PENDING, ACTIVE, SUSPENDED_*, CANCELED
- **Vínculo com PPPoE** - Associação automática usuário ↔ contrato
- **Eventos de Mudança de Status** - Dispara integrações de rede
- **Multi-tenant** - Isolamento por empresa
- **Histórico de Status** - Auditoria de mudanças

### 🚧 Roadmap
- [ ] Contratos com múltiplos serviços (bundle)
- [ ] Alteração de plano (upgrade/downgrade)
- [ ] Contrato com data de término definida
- [ ] Renovação automática
- [ ] Cálculo proporcional de valores

---

## 🔗 Referências Relacionadas

**Arquitetura:**
- [../ARCHITECTURE_ACTUAL.md](../ARCHITECTURE_ACTUAL.md) - Arquitetura do sistema

**Outras Features:**
- [../customers/](../customers/) - Clientes vinculados aos contratos
- [../invoices/](../invoices/) - Faturamento baseado em contratos
- [../network/](../network/) - Bloqueio/desbloqueio baseado em status
- [../financial/](../financial/) - Lançamentos financeiros

**Código:**
- `ContractService.java` - Lógica de negócio
- `ServicePlanService.java` - Gestão de planos
- `ContractStatusChangedEvent.java` - Eventos de mudança

---

## 🛠️ Endpoints Principais

```
GET    /api/contracts               - Listar contratos
POST   /api/contracts               - Criar contrato
GET    /api/contracts/{id}          - Buscar contrato
PUT    /api/contracts/{id}          - Atualizar contrato
DELETE /api/contracts/{id}          - Cancelar contrato

PUT    /api/contracts/{id}/suspend-financial  - Suspender por inadimplência
PUT    /api/contracts/{id}/suspend-request    - Suspender por solicitação
PUT    /api/contracts/{id}/activate           - Reativar contrato

GET    /api/service-plans           - Listar planos
POST   /api/service-plans           - Criar plano
PUT    /api/service-plans/{id}      - Atualizar plano
```

---

## 📊 Status de Contratos

| Status | Descrição | Ações Permitidas |
|--------|-----------|------------------|
| `PENDING` | Aguardando ativação | Ativar, Cancelar |
| `ACTIVE` | Ativo e funcionando | Suspender, Cancelar |
| `SUSPENDED_FINANCIAL` | Suspenso por inadimplência | Reativar (após pagamento) |
| `SUSPENDED_REQUEST` | Suspenso por solicitação | Reativar, Cancelar |
| `CANCELED` | Cancelado definitivamente | Nenhuma |

---

## 🔄 Integração com Outras Features

### Faturamento
- Contrato ACTIVE gera fatura mensalmente
- Contrato CANCELED não gera mais faturas

### Rede (Mikrotik)
- Mudança de status dispara evento `ContractStatusChangedEvent`
- NetworkIntegrationService processa bloqueio/desbloqueio assíncronamente

### Financeiro
- Ativação de contrato pode gerar taxa de instalação
- Cancelamento pode gerar multa rescisória

---

## 💡 Regras de Negócio

### Criação de Contrato
- ✅ Requer cliente válido
- ✅ Requer plano de serviço válido
- ✅ Status inicial: PENDING
- ✅ Pode vincular usuário PPPoE existente ou criar novo

### Suspensão
- ✅ Apenas contratos ACTIVE podem ser suspensos
- ✅ Suspensão dispara bloqueio no Mikrotik (assíncrono)
- ✅ Tipo FINANCIAL é automático (via job)
- ✅ Tipo REQUEST é manual (solicitação do cliente)

### Reativação
- ✅ Apenas contratos SUSPENDED_* podem ser reativados
- ✅ Reativação dispara desbloqueio no Mikrotik (assíncrono)
- ✅ FINANCIAL requer fatura paga
- ✅ REQUEST não requer validação adicional

### Cancelamento
- ✅ Não pode ser revertido
- ✅ Usuário PPPoE é removido do Mikrotik (se existir)
- ✅ Faturas pendentes permanecem (cobrança)

---

## 🧪 Testes

```bash
# Criar contrato
curl -X POST http://localhost:8080/api/contracts \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "servicePlanId": 2,
    "installationDate": "2026-02-18"
  }'

# Suspender por inadimplência
curl -X PUT http://localhost:8080/api/contracts/1/suspend-financial \
  -H "Authorization: Bearer TOKEN"

# Reativar
curl -X PUT http://localhost:8080/api/contracts/1/activate \
  -H "Authorization: Bearer TOKEN"
```

---

**📅 Última atualização:** 2026-02-18  
**👤 Responsável:** Backend Team  
**🔄 Status:** ✅ Produção

