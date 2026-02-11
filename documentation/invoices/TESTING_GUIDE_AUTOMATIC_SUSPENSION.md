# 🧪 Guia de Teste: Suspensão Automática de Contratos

## ⚠️ ANTES DE COMEÇAR

Este guia pressupõe que você vai **EXCLUIR E RECRIAR** o banco de dados conforme mencionado.

---

## 📋 Pré-requisitos

- ✅ MySQL rodando
- ✅ Perfil "BLOQUEADO" criado no Mikrotik
- ✅ Código compilado sem erros

---

## 🔄 Passo 1: Recriar Banco de Dados

```bash
# Conectar ao MySQL
mysql -u root -p

# Executar comandos
DROP DATABASE IF EXISTS mikrotik;
CREATE DATABASE mikrotik CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mikrotik;
exit
```

---

## 🚀 Passo 2: Iniciar Aplicação

```bash
cd /home/tiago/workspace-intelij-idea/mikrotik
mvn spring-boot:run
```

**Aguarde mensagem:**
```
Migration V1__add_multi_tenant_support.sql - SUCCESS
Application started on port 8080
```

---

## ✅ Passo 3: Verificar Estrutura

### 3.1 Verificar campo `suspension_days` foi criado:
```sql
mysql -u root -p mikrotik

DESCRIBE companies;

# Deve aparecer:
# | suspension_days | int | NO | | 5 | Dias de tolerância... |
```

### 3.2 Verificar empresa foi criada com valor padrão:
```sql
SELECT id, name, suspension_days FROM companies;

# Resultado esperado:
# +----+------------------+-----------------+
# | id | name             | suspension_days |
# +----+------------------+-----------------+
# | 1  | Flash-Net Telecom| 5               |
# +----+------------------+-----------------+
```

---

## 🔧 Passo 4: Configurar Dias de Tolerância (Opcional)

### 4.1 Via API REST:
```http
PATCH http://localhost:8080/api/companies/1
Authorization: Bearer {SEU_TOKEN}
Content-Type: application/json

{
  "suspensionDays": 3
}
```

### 4.2 Via SQL direto:
```sql
UPDATE companies SET suspension_days = 3 WHERE id = 1;
```

### 4.3 Verificar alteração:
```http
GET http://localhost:8080/api/companies/1
Authorization: Bearer {SEU_TOKEN}

# Resposta esperada:
{
  "id": 1,
  "name": "Flash-Net Telecom",
  "suspensionDays": 3,  // ✅ Atualizado
  ...
}
```

---

## 📊 Passo 5: Criar Cenário de Teste

### 5.1 Criar cliente
```http
POST http://localhost:8080/api/customers
Authorization: Bearer {SEU_TOKEN}
Content-Type: application/json

{
  "name": "Cliente Teste Inadimplente",
  "type": "FISICA",
  "document": "123.456.789-00",
  "email": "teste@email.com",
  "status": "ACTIVE"
}

# Anotar: customerId retornado (ex: 1)
```

### 5.2 Criar contrato ATIVO
```http
POST http://localhost:8080/api/contracts
Authorization: Bearer {SEU_TOKEN}
Content-Type: application/json

{
  "customerId": 1,
  "servicePlanId": 1,  // Plano existente
  "billingDay": 10,
  "amount": 100.00,
  "startDate": "2026-01-01"
}

# Anotar: contractId retornado (ex: 1)
```

### 5.3 Ativar contrato (cria PPPoE no Mikrotik)
```http
PATCH http://localhost:8080/api/contracts/1/activate
Authorization: Bearer {SEU_TOKEN}

# Resposta esperada:
{
  "id": 1,
  "status": "ACTIVE",  // ✅
  "pppoeUserId": 123,  // ✅ Criado
  ...
}
```

### 5.4 Criar fatura VENCIDA
```http
POST http://localhost:8080/api/invoices
Authorization: Bearer {SEU_TOKEN}
Content-Type: application/json

{
  "contractId": 1,
  "customerId": 1,
  "description": "Teste - Mensalidade Janeiro",
  "referenceMonth": "2026-01-01",
  "dueDate": "2026-01-20",  // 6 dias atrás (se hoje é 26/01)
  "originalAmount": 100.00,
  "finalAmount": 100.00,
  "status": "OVERDUE"
}
```

---

## ⏰ Passo 6: Testar Suspensão Automática

### Opção A: Aguardar job (03:00 AM)
- Aguarde até às 03:00 da madrugada
- Verifique logs da aplicação

### Opção B: Executar job manualmente (RECOMENDADO)

#### 6.1 Criar endpoint de teste no `InvoiceBillingJob.java`:
```java
// Adicionar temporariamente para testes
@GetMapping("/api/test/run-suspension")
public ResponseEntity<String> testSuspension() {
    suspendOverdueContracts();
    return ResponseEntity.ok("Job executado");
}
```

#### 6.2 Executar:
```http
GET http://localhost:8080/api/test/run-suspension
Authorization: Bearer {SEU_TOKEN}
```

---

## 🔍 Passo 7: Verificar Resultados

### 7.1 Verificar contrato foi suspenso:
```http
GET http://localhost:8080/api/contracts/1
Authorization: Bearer {SEU_TOKEN}

# Resposta esperada:
{
  "id": 1,
  "status": "SUSPENDED_FINANCIAL",  // ✅ Suspenso
  "pppoeUser": {
    "status": "DISABLED"  // ✅ Desabilitado
  }
}
```

### 7.2 Verificar no banco de dados:
```sql
SELECT id, status FROM contracts WHERE id = 1;
# Resultado: status = SUSPENDED_FINANCIAL

SELECT id, username, status FROM pppoe_users WHERE id = 
  (SELECT pppoe_user_id FROM contracts WHERE id = 1);
# Resultado: status = DISABLED
```

### 7.3 Verificar no Mikrotik (via SSH):
```bash
# Conectar via SSH no Mikrotik
ssh admin@100.64.255.2

# Verificar perfil do usuário
/ppp secret print detail where name="usuarioteste"

# Resultado esperado:
# profile=BLOQUEADO  ✅

# Verificar se foi desconectado
/ppp active print
# Não deve aparecer o usuário (foi desconectado)
```

---

## 📋 Passo 8: Verificar Logs

### 8.1 Logs esperados no console da aplicação:
```log
==========================================================
SUSPENSÃO AUTOMÁTICA DE CONTRATOS POR INADIMPLÊNCIA
Data/Hora: 2026-01-26 03:00:00
==========================================================
----------------------------------------------------------
Processando empresa: Flash-Net Telecom (ID: 1)
Dias de tolerância configurados: 5 dias
----------------------------------------------------------
Data atual: 2026-01-26
Data limite para suspensão: 2026-01-21 (faturas vencidas até esta data)
Encontrados 1 contratos para suspensão
  🔒 Suspendendo contrato 1 - Cliente: Cliente Teste Inadimplente
  === SUSPENDENDO CONTRATO POR INADIMPLÊNCIA - ID: 1 ===
  Status alterado para: SUSPENDED_FINANCIAL
  Chamando blockUserInMikrotik...
  >>> BLOQUEANDO USUÁRIO NO MIKROTIK - Contrato ID: 1 <<<
  Company ID: 1
  Contrato encontrado - pppoeUserId: 123
  Buscando usuário PPPoE ID: 123
  Usuário PPPoE encontrado: usuarioteste
  Servidor Mikrotik: Flash-Net (100.64.255.2:22)
  >>> PASSO 1: Alterando perfil para 'BLOQUEADO' <<<
  ✅ Perfil alterado com sucesso no Mikrotik
  >>> PASSO 2: Desconectando usuário ativo <<<
  ✅ Usuário desconectado com sucesso
  >>> PASSO 3: Atualizando status no banco <<<
  ✅ Status atualizado no banco: DISABLED
  ✅ USUÁRIO PPPoE usuarioteste BLOQUEADO COM SUCESSO NO MIKROTIK
  === SUSPENSÃO CONCLUÍDA ===
  ✅ Contrato 1 suspenso e bloqueado no Mikrotik com sucesso
----------------------------------------------------------
Empresa Flash-Net Telecom: Resumo da suspensão automática
  • Contratos para processar: 1
  • ✅ Suspensos com sucesso: 1
  • ℹ️  Já estavam suspensos: 0
  • ❌ Erros: 0
----------------------------------------------------------
==========================================================
SUSPENSÃO AUTOMÁTICA CONCLUÍDA
==========================================================
```

---

## 🔄 Passo 9: Testar Reativação

### 9.1 "Pagar" a fatura:
```http
PATCH http://localhost:8080/api/invoices/{invoiceId}/status
Authorization: Bearer {SEU_TOKEN}
Content-Type: application/json

{
  "status": "PAID"
}
```

### 9.2 Reativar contrato:
```http
PATCH http://localhost:8080/api/contracts/1/activate
Authorization: Bearer {SEU_TOKEN}

# Resultado esperado:
{
  "id": 1,
  "status": "ACTIVE",  // ✅ Reativado
  "pppoeUser": {
    "status": "OFFLINE"  // ✅ Desbloqueado
  }
}
```

### 9.3 Verificar no Mikrotik:
```bash
/ppp secret print detail where name="usuarioteste"
# profile=40MB  ✅ Perfil original restaurado
```

---

## ✅ Checklist de Validação

- [ ] Campo `suspension_days` existe na tabela `companies`
- [ ] Empresa tem `suspension_days = 5` (ou valor configurado)
- [ ] Contrato criado e ativado (status = ACTIVE)
- [ ] Fatura vencida criada (status = OVERDUE, vencida há 6+ dias)
- [ ] Job executado (manualmente ou às 03:00)
- [ ] Contrato suspenso (status = SUSPENDED_FINANCIAL)
- [ ] PPPoE bloqueado (status = DISABLED)
- [ ] Perfil Mikrotik = "BLOQUEADO"
- [ ] Conexão ativa removida
- [ ] Logs detalhados exibidos
- [ ] Reativação funciona corretamente

---

## 🐛 Troubleshooting

### Problema: Job não executa
**Solução:** Verificar se `@EnableScheduling` está no `MikrotikApplication.java`

### Problema: Perfil "BLOQUEADO" não existe no Mikrotik
**Solução:**
```bash
ssh admin@100.64.255.2
/ppp profile add name=BLOQUEADO rate-limit=64k/64k
```

### Problema: Contrato não é suspenso
**Causas possíveis:**
1. Contrato não está ACTIVE
2. Fatura não está OVERDUE
3. Fatura venceu há menos de X dias (verificar `suspension_days`)
4. Erro no Mikrotik (ver logs)

### Problema: Erro ao bloquear no Mikrotik
**Solução:** Verificar logs detalhados, credenciais SSH, conectividade

---

## 📞 Suporte

- **Logs:** Console da aplicação Spring Boot
- **Jobs:** Executam automaticamente às 01:00, 02:00 e 03:00
- **Configuração:** Via API `/api/companies/{id}`

---

**Bons testes! 🚀**
