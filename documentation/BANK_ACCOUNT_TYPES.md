# 🏦 Tipos de Contas Bancárias - Referência para Front-End
## ✅ VALORES ACEITOS (AccountType)
O campo `accountType` aceita **APENAS** os seguintes valores:
```json
{
  "accountType": "CHECKING"       // ✅ Conta Corrente
}
{
  "accountType": "SAVINGS"        // ✅ Poupança
}
{
  "accountType": "CASH"           // ✅ Caixa Geral
}
{
  "accountType": "CASH_INTERNAL"  // ✅ Caixa Interno (NEW!)
}
{
  "accountType": "DIGITAL_WALLET" // ✅ Carteira Digital
}
{
  "accountType": "CREDIT_CARD"    // ✅ Cartão de Crédito
}
```
---
## 📋 EXEMPLO DE REQUEST CORRETO
### POST /api/bank-accounts
```json
{
  "name": "Caixa Interno",
  "accountType": "CASH_INTERNAL",
  "initialBalance": 1000.00,
  "active": true,
  "notes": "Caixa para uso interno da empresa"
}
```
---
## ❌ ERRO COMUM
### Request Errado:
```json
{
  "accountType": "cash_internal"  // ❌ Minúsculas não funcionam!
}
{
  "accountType": "CashInternal"   // ❌ CamelCase não funciona!
}
{
  "accountType": "CASH_EXTERNA"   // ❌ Valor não existe!
}
```
### Erro Retornado:
```json
{
  "timestamp": "2026-02-09T22:29:36",
  "status": 400,
  "error": "Bad Request",
  "message": "JSON parse error: Cannot deserialize value of type `br.com.mikrotik.model.BankAccount$AccountType` from String \"CASH_EXTERNA\": not one of the values accepted for Enum class: [DIGITAL_WALLET, SAVINGS, CHECKING, CASH, CASH_INTERNAL, CREDIT_CARD]"
}
```
---
## 🎨 SUGESTÕES PARA O FRONT-END
### Select/Dropdown de Tipos
```jsx
<select name="accountType" required>
  <option value="">Selecione o tipo...</option>
  <option value="CHECKING">Conta Corrente</option>
  <option value="SAVINGS">Poupança</option>
  <option value="CASH">Caixa Geral</option>
  <option value="CASH_INTERNAL">Caixa Interno</option>
  <option value="DIGITAL_WALLET">Carteira Digital (PIX, PicPay, etc)</option>
  <option value="CREDIT_CARD">Cartão de Crédito</option>
</select>
```
### Constantes JavaScript
```javascript
export const BANK_ACCOUNT_TYPES = {
  CHECKING: 'CHECKING',
  SAVINGS: 'SAVINGS',
  CASH: 'CASH',
  CASH_INTERNAL: 'CASH_INTERNAL',
  DIGITAL_WALLET: 'DIGITAL_WALLET',
  CREDIT_CARD: 'CREDIT_CARD'
};
export const BANK_ACCOUNT_LABELS = {
  CHECKING: 'Conta Corrente',
  SAVINGS: 'Poupança',
  CASH: 'Caixa Geral',
  CASH_INTERNAL: 'Caixa Interno',
  DIGITAL_WALLET: 'Carteira Digital',
  CREDIT_CARD: 'Cartão de Crédito'
};
```
### Ícones Sugeridos
```javascript
export const BANK_ACCOUNT_ICONS = {
  CHECKING: '🏦',      // Banco
  SAVINGS: '🐷',       // Cofre/Poupança
  CASH: '💵',          // Dinheiro
  CASH_INTERNAL: '💰', // Caixa Interno
  DIGITAL_WALLET: '📱', // Carteira Digital
  CREDIT_CARD: '💳'    // Cartão
};
```
### Cores Sugeridas
```javascript
export const BANK_ACCOUNT_COLORS = {
  CHECKING: '#007bff',      // Azul
  SAVINGS: '#28a745',       // Verde
  CASH: '#ffc107',          // Amarelo
  CASH_INTERNAL: '#fd7e14', // Laranja
  DIGITAL_WALLET: '#6f42c1', // Roxo
  CREDIT_CARD: '#dc3545'    // Vermelho
};
```
---
## 🔍 VALIDAÇÃO NO FRONT-END
```javascript
function isValidAccountType(type) {
  const validTypes = [
    'CHECKING',
    'SAVINGS',
    'CASH',
    'CASH_INTERNAL',
    'DIGITAL_WALLET',
    'CREDIT_CARD'
  ];
  return validTypes.includes(type);
}
// Uso
const formData = {
  name: "Caixa Interno",
  accountType: "CASH_INTERNAL"
};
if (!isValidAccountType(formData.accountType)) {
  alert('Tipo de conta inválido!');
  return;
}
// Prosseguir com envio
```
---
## 📊 DIFERENÇA ENTRE CASH E CASH_INTERNAL
| Tipo | Uso Recomendado | Exemplo |
|------|----------------|---------|
| `CASH` | Caixa geral da empresa, aceita pagamentos de clientes | "Caixa Sede", "Caixa Atendimento" |
| `CASH_INTERNAL` | Caixa para uso interno, despesas operacionais | "Caixa Pequenas Despesas", "Caixa Técnicos" |
**Diferença prática:**
- **CASH**: Usado para registrar recebimentos de clientes (faturas pagas em dinheiro)
- **CASH_INTERNAL**: Usado para despesas do dia a dia (compra de material, combustível, etc)
---
## ✅ CHECKLIST DE VALIDAÇÃO
Antes de enviar para `/api/bank-accounts`:
- [ ] `accountType` está em MAIÚSCULAS
- [ ] `accountType` é um dos 6 valores aceitos
- [ ] `name` está preenchido (obrigatório)
- [ ] `initialBalance` é um número >= 0
- [ ] `active` é boolean (true/false)
---
## 🚀 EXEMPLO COMPLETO (React)
```jsx
import { useState } from 'react';
const ACCOUNT_TYPES = [
  { value: 'CHECKING', label: 'Conta Corrente', icon: '🏦' },
  { value: 'SAVINGS', label: 'Poupança', icon: '🐷' },
  { value: 'CASH', label: 'Caixa Geral', icon: '💵' },
  { value: 'CASH_INTERNAL', label: 'Caixa Interno', icon: '💰' },
  { value: 'DIGITAL_WALLET', label: 'Carteira Digital', icon: '📱' },
  { value: 'CREDIT_CARD', label: 'Cartão de Crédito', icon: '💳' }
];
function CreateBankAccountForm() {
  const [formData, setFormData] = useState({
    name: '',
    accountType: '',
    bankCode: '',
    agency: '',
    accountNumber: '',
    initialBalance: 0,
    active: true,
    notes: ''
  });
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/bank-accounts', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'X-Company-Id': companyId,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });
      if (!response.ok) {
        const error = await response.json();
        alert(`Erro: ${error.message}`);
        return;
      }
      const newAccount = await response.json();
      alert('Conta criada com sucesso!');
      console.log(newAccount);
    } catch (error) {
      alert('Erro ao criar conta bancária');
      console.error(error);
    }
  };
  return (
    <form onSubmit={handleSubmit}>
      <div className="mb-3">
        <label className="form-label">Nome da Conta *</label>
        <input
          type="text"
          className="form-control"
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
          required
        />
      </div>
      <div className="mb-3">
        <label className="form-label">Tipo de Conta *</label>
        <select
          className="form-select"
          value={formData.accountType}
          onChange={(e) => setFormData({...formData, accountType: e.target.value})}
          required
        >
          <option value="">Selecione...</option>
          {ACCOUNT_TYPES.map(type => (
            <option key={type.value} value={type.value}>
              {type.icon} {type.label}
            </option>
          ))}
        </select>
      </div>
      <div className="mb-3">
        <label className="form-label">Saldo Inicial</label>
        <input
          type="number"
          step="0.01"
          min="0"
          className="form-control"
          value={formData.initialBalance}
          onChange={(e) => setFormData({...formData, initialBalance: parseFloat(e.target.value)})}
        />
      </div>
      <div className="mb-3">
        <label className="form-label">Observações</label>
        <textarea
          className="form-control"
          rows="3"
          value={formData.notes}
          onChange={(e) => setFormData({...formData, notes: e.target.value})}
        />
      </div>
      <button type="submit" className="btn btn-primary">
        Criar Conta Bancária
      </button>
    </form>
  );
}
```
---
**Atualizado em:** 2026-02-09  
**Versão:** 1.1 (adicionado CASH_INTERNAL)
