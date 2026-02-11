# 📊 Guia Completo da API de Fluxo de Caixa

## 🎯 Visão Geral

Este documento contém **TODOS os endpoints** do módulo de Fluxo de Caixa, incluindo exemplos de requisição e resposta para o frontend.

---

## 🏦 1. Contas Bancárias (`/api/bank-accounts`)

### 1.1. Criar Nova Conta Bancária

**Endpoint:** `POST /api/bank-accounts`

**Autenticação:** Obrigatória (JWT)

**Request Body:**
```json
{
  "name": "Caixa Interno Escritório",
  "accountType": "CASH_INTERNAL",
  "bankCode": null,
  "agency": null,
  "accountNumber": null,
  "initialBalance": 5000.00,
  "active": true,
  "notes": "Caixa para despesas operacionais"
}
```

**Tipos de Conta Válidos:**
- `CHECKING` - Conta Corrente
- `SAVINGS` - Poupança
- `CASH` - Caixa Geral
- `CASH_INTERNAL` - Caixa Interno (uso da empresa)
- `DIGITAL_WALLET` - Carteira Digital (PicPay, Mercado Pago)
- `CREDIT_CARD` - Cartão de Crédito

**Response (201 Created):**
```json
{
  "id": 1,
  "companyId": 1,
  "name": "Caixa Interno Escritório",
  "accountType": "CASH_INTERNAL",
  "bankCode": null,
  "agency": null,
  "accountNumber": null,
  "initialBalance": 5000.00,
  "currentBalance": 5000.00,
  "active": true,
  "notes": "Caixa para despesas operacionais",
  "createdAt": "2026-02-10T08:30:00",
  "updatedAt": "2026-02-10T08:30:00"
}
```

---

### 1.2. Listar Todas as Contas

**Endpoint:** `GET /api/bank-accounts`

**Query Parameters:**
- `page` (opcional) - Número da página (default: 0)
- `size` (opcional) - Tamanho da página (default: 20)
- `sort` (opcional) - Campo de ordenação (ex: `name,asc`)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "name": "Caixa Interno Escritório",
      "accountType": "CASH_INTERNAL",
      "currentBalance": 5000.00,
      "active": true
    },
    {
      "id": 2,
      "companyId": 1,
      "name": "Banco do Brasil - CC 12345-6",
      "accountType": "CHECKING",
      "bankCode": "001",
      "agency": "1234",
      "accountNumber": "12345-6",
      "currentBalance": 15000.00,
      "active": true
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

### 1.3. Buscar Conta por ID

**Endpoint:** `GET /api/bank-accounts/{id}`

**Response (200 OK):**
```json
{
  "id": 1,
  "companyId": 1,
  "name": "Caixa Interno Escritório",
  "accountType": "CASH_INTERNAL",
  "initialBalance": 5000.00,
  "currentBalance": 5230.50,
  "active": true,
  "createdAt": "2026-02-10T08:30:00",
  "updatedAt": "2026-02-10T10:15:00"
}
```

---

### 1.4. Atualizar Conta

**Endpoint:** `PUT /api/bank-accounts/{id}`

**Request Body (apenas campos que deseja alterar):**
```json
{
  "name": "Caixa Interno - Escritório Principal",
  "notes": "Atualizado: Apenas para despesas aprovadas pela gerência"
}
```

**⚠️ IMPORTANTE:** Não é possível alterar `currentBalance` diretamente. Use lançamentos financeiros.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Caixa Interno - Escritório Principal",
  "accountType": "CASH_INTERNAL",
  "currentBalance": 5230.50,
  "active": true,
  "notes": "Atualizado: Apenas para despesas aprovadas pela gerência"
}
```

---

### 1.5. Inativar Conta (Soft Delete)

**Endpoint:** `DELETE /api/bank-accounts/{id}`

**Response (204 No Content)**

> A conta não é deletada, apenas marcada como `active = false`.

---

### 1.6. Listar Contas Ativas

**Endpoint:** `GET /api/bank-accounts/active`

**Query Parameters:** `page`, `size`, `sort`

---

### 1.7. Filtrar por Tipo de Conta

**Endpoint:** `GET /api/bank-accounts/type/{accountType}`

**Exemplo:** `GET /api/bank-accounts/type/CHECKING`

---

## 📊 2. Plano de Contas (`/api/chart-of-accounts`)

### 2.1. Criar Categoria Contábil

**Endpoint:** `POST /api/chart-of-accounts`

**Request Body:**
```json
{
  "code": "3.1.01",
  "name": "Receita de Mensalidades - Internet",
  "accountType": "REVENUE",
  "category": "SUBSCRIPTION_REVENUE",
  "parentId": null,
  "active": true
}
```

**Tipos de Conta:**
- `REVENUE` - Receita
- `EXPENSE` - Despesa
- `ASSET` - Ativo
- `LIABILITY` - Passivo
- `EQUITY` - Patrimônio Líquido

**Categorias Disponíveis:**

**Receitas:**
- `SUBSCRIPTION_REVENUE` - Receita de Assinaturas/Mensalidades
- `INSTALLATION_FEE` - Taxa de Instalação
- `LATE_FEE` - Multas e Juros
- `OTHER_REVENUE` - Outras Receitas

**Despesas:**
- `LINK_COST` - Custo de Links/Internet
- `SALARY` - Salários e Encargos
- `RENT` - Aluguel
- `MARKETING` - Marketing e Publicidade
- `MAINTENANCE` - Manutenção
- `TAX` - Impostos
- `OTHER_EXPENSE` - Outras Despesas

**Ativos:**
- `CASH` - Caixa
- `BANK` - Banco
- `ACCOUNTS_RECEIVABLE` - Contas a Receber

**Passivos:**
- `ACCOUNTS_PAYABLE` - Contas a Pagar
- `LOAN` - Empréstimos

**Patrimônio:**
- `CAPITAL` - Capital Social

---

### 2.2. Listar Plano de Contas

**Endpoint:** `GET /api/chart-of-accounts`

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "code": "3.1.01",
      "name": "Receita de Mensalidades - Internet",
      "accountType": "REVENUE",
      "category": "SUBSCRIPTION_REVENUE",
      "active": true
    },
    {
      "id": 2,
      "code": "4.1.01",
      "name": "Custo de Link - Provedor Tier 1",
      "accountType": "EXPENSE",
      "category": "LINK_COST",
      "active": true
    }
  ]
}
```

---

### 2.3. Filtrar por Tipo

**Endpoint:** `GET /api/chart-of-accounts/type/{accountType}`

**Exemplo:** `GET /api/chart-of-accounts/type/REVENUE`

---

### 2.4. Filtrar por Categoria

**Endpoint:** `GET /api/chart-of-accounts/category/{category}`

**Exemplo:** `GET /api/chart-of-accounts/category/SUBSCRIPTION_REVENUE`

---

## 💰 3. Lançamentos Financeiros (`/api/financial-entries`)

### 3.1. Criar Lançamento Manual

**Endpoint:** `POST /api/financial-entries`

**Request Body (Despesa):**
```json
{
  "bankAccountId": 1,
  "chartOfAccountId": 5,
  "entryType": "DEBIT",
  "transactionType": "MANUAL_ENTRY",
  "amount": 350.00,
  "description": "Pagamento de aluguel - Fevereiro/2026",
  "referenceDate": "2026-02-10",
  "effectiveDate": "2026-02-10T14:30:00",
  "notes": "Aluguel do escritório"
}
```

**Request Body (Receita):**
```json
{
  "bankAccountId": 2,
  "chartOfAccountId": 1,
  "entryType": "CREDIT",
  "transactionType": "MANUAL_ENTRY",
  "amount": 1500.00,
  "description": "Recebimento de taxa de instalação",
  "referenceDate": "2026-02-10",
  "effectiveDate": "2026-02-10T15:00:00",
  "notes": "Cliente João Silva - Contrato #123"
}
```

**Tipos de Lançamento:**
- `CREDIT` - Entrada de dinheiro (aumenta saldo)
- `DEBIT` - Saída de dinheiro (diminui saldo)
- `REVERSAL` - Estorno

**Tipos de Transação:**
- `INVOICE_PAYMENT` - Pagamento de Fatura (criado automaticamente)
- `MANUAL_ENTRY` - Lançamento Manual (despesas operacionais)
- `TRANSFER` - Transferência entre contas
- `ADJUSTMENT` - Ajuste de saldo
- `REFUND` - Reembolso

**Response (201 Created):**
```json
{
  "id": 15,
  "companyId": 1,
  "bankAccountId": 1,
  "chartOfAccountId": 5,
  "entryType": "DEBIT",
  "transactionType": "MANUAL_ENTRY",
  "amount": 350.00,
  "description": "Pagamento de aluguel - Fevereiro/2026",
  "referenceDate": "2026-02-10",
  "effectiveDate": "2026-02-10T14:30:00",
  "status": "ACTIVE",
  "notes": "Aluguel do escritório",
  "createdAt": "2026-02-10T14:30:00"
}
```

---

### 3.2. Listar Lançamentos

**Endpoint:** `GET /api/financial-entries`

**Query Parameters:**
- `page` - Número da página
- `size` - Tamanho da página
- `sort` - Ordenação (ex: `effectiveDate,desc`)

---

### 3.3. Filtrar por Conta Bancária

**Endpoint:** `GET /api/financial-entries/account/{accountId}`

**Exemplo:** `GET /api/financial-entries/account/1?page=0&size=50`

---

### 3.4. Filtrar por Período

**Endpoint:** `GET /api/financial-entries/period`

**Query Parameters:**
- `startDate` - Data inicial (formato: YYYY-MM-DD)
- `endDate` - Data final (formato: YYYY-MM-DD)
- `page`, `size`, `sort`

**Exemplo:** `GET /api/financial-entries/period?startDate=2026-02-01&endDate=2026-02-28`

---

### 3.5. Estornar Lançamento

**Endpoint:** `POST /api/financial-entries/{id}/reverse`

**Request Body:**
```json
{
  "reason": "Pagamento duplicado por erro no sistema",
  "userId": 1
}
```

**Response (200 OK):**
```json
{
  "id": 16,
  "entryType": "CREDIT",
  "transactionType": "REFUND",
  "amount": 350.00,
  "description": "ESTORNO: Pagamento de aluguel - Fevereiro/2026 - Motivo: Pagamento duplicado por erro no sistema",
  "reversedFromId": 15,
  "status": "ACTIVE"
}
```

> O lançamento original terá seu `status` alterado para `REVERSED`.

---

## 📈 4. Endpoints de Dashboard e Relatórios

### 4.1. Visão Geral das Contas

**Endpoint:** `GET /api/cash-flow/accounts-overview`

**Response:**
```json
{
  "totalAccounts": 5,
  "totalBalance": 45230.50,
  "accounts": [
    {
      "id": 1,
      "name": "Caixa Interno Escritório",
      "accountType": "CASH_INTERNAL",
      "currentBalance": 5230.50
    },
    {
      "id": 2,
      "name": "Banco do Brasil - CC 12345-6",
      "accountType": "CHECKING",
      "currentBalance": 40000.00
    }
  ]
}
```

---

### 4.2. Estatísticas do Período

**Endpoint:** `GET /api/cash-flow/stats`

**Query Parameters:**
- `startDate` - Data inicial (YYYY-MM-DD)
- `endDate` - Data final (YYYY-MM-DD)

**Exemplo:** `GET /api/cash-flow/stats?startDate=2026-02-01&endDate=2026-02-28`

**Response:**
```json
{
  "period": {
    "start": "2026-02-01",
    "end": "2026-02-28"
  },
  "totalRevenue": 85000.00,
  "totalExpenses": 32500.00,
  "netProfit": 52500.00,
  "topExpenseCategories": [
    {
      "category": "LINK_COST",
      "amount": 15000.00,
      "percentage": 46.15
    },
    {
      "category": "SALARY",
      "amount": 12000.00,
      "percentage": 36.92
    }
  ],
  "revenueByCategory": [
    {
      "category": "SUBSCRIPTION_REVENUE",
      "amount": 80000.00,
      "percentage": 94.12
    },
    {
      "category": "INSTALLATION_FEE",
      "amount": 5000.00,
      "percentage": 5.88
    }
  ]
}
```

---

### 4.3. Fluxo de Caixa Diário

**Endpoint:** `GET /api/cash-flow/daily`

**Query Parameters:**
- `startDate` - Data inicial
- `endDate` - Data final

**Response:**
```json
{
  "dailyFlow": [
    {
      "date": "2026-02-01",
      "openingBalance": 40000.00,
      "totalCredits": 3500.00,
      "totalDebits": 1200.00,
      "closingBalance": 42300.00
    },
    {
      "date": "2026-02-02",
      "openingBalance": 42300.00,
      "totalCredits": 2800.00,
      "totalDebits": 900.00,
      "closingBalance": 44200.00
    }
  ]
}
```

---

## 🔄 5. Exemplos de Fluxos Completos

### 5.1. Registrar Despesa Operacional

```javascript
// 1. Criar lançamento de despesa
const despesa = await fetch('/api/financial-entries', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN',
    'Content-Type': 'application/json',
    'X-Company-Id': '1'
  },
  body: JSON.stringify({
    bankAccountId: 1,
    chartOfAccountId: 5, // Categoria: RENT
    entryType: 'DEBIT',
    transactionType: 'MANUAL_ENTRY',
    amount: 1500.00,
    description: 'Aluguel - Março/2026',
    referenceDate: '2026-03-10',
    effectiveDate: '2026-03-10T10:00:00'
  })
});

// 2. Verificar saldo atualizado da conta
const conta = await fetch('/api/bank-accounts/1', {
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN',
    'X-Company-Id': '1'
  }
});

console.log('Novo saldo:', conta.currentBalance);
```

---

### 5.2. Gerar Relatório Mensal

```javascript
const relatorio = await fetch('/api/cash-flow/stats?startDate=2026-02-01&endDate=2026-02-28', {
  headers: {
    'Authorization': 'Bearer YOUR_JWT_TOKEN',
    'X-Company-Id': '1'
  }
});

const data = await relatorio.json();

console.log('Receita Total:', data.totalRevenue);
console.log('Despesas Totais:', data.totalExpenses);
console.log('Lucro Líquido:', data.netProfit);
```

---

## ⚠️ Regras de Negócio Importantes

### 1. Multi-tenant (Isolamento de Dados)
- **SEMPRE** enviar o header `X-Company-Id` em todas as requisições
- Cada empresa vê apenas seus próprios dados

### 2. Alteração de Saldo
- **NUNCA** alterar `currentBalance` diretamente via PUT
- Sempre usar lançamentos financeiros (`POST /api/financial-entries`)
- O saldo é atualizado automaticamente com **Lock Pessimista** para evitar race conditions

### 3. Estornos
- Lançamentos não são deletados, apenas estornados
- Cria-se um lançamento de `REVERSAL` referenciando o original
- O lançamento original tem status alterado para `REVERSED`

### 4. Validações
- `amount` deve ser positivo
- `referenceDate` e `effectiveDate` são obrigatórios
- `bankAccountId` e `chartOfAccountId` devem existir e pertencer à empresa

---

## 🔐 Autenticação

Todas as requisições requerem:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Company-Id: 1
Content-Type: application/json
```

---

## 📌 Status Codes

- `200 OK` - Sucesso (GET, PUT)
- `201 Created` - Recurso criado (POST)
- `204 No Content` - Sucesso sem retorno (DELETE)
- `400 Bad Request` - Dados inválidos
- `401 Unauthorized` - Não autenticado
- `403 Forbidden` - Sem permissão
- `404 Not Found` - Recurso não encontrado
- `409 Conflict` - Conflito de dados (ex: duplicação)
- `500 Internal Server Error` - Erro no servidor

---

## 🎯 Próximos Passos (Roadmap)

- [ ] Job de consolidação diária de saldos
- [ ] Projeção de receita baseada em contratos
- [ ] Exportação para Excel/PDF
- [ ] Gráficos de DRE (Demonstrativo de Resultados)
- [ ] Análise de Churn Financeiro

---

**Última atualização:** 2026-02-10  
**Versão da API:** 1.0.0

