# 🧪 TESTE MANUAL: Suspensão Automática de Contratos

## ✅ Implementação Concluída

Foi adicionado um **endpoint temporário** para testar a suspensão automática manualmente.

---

## 📍 Endpoint de Teste

```
POST http://localhost:8080/api/test/trigger-suspension
```

### Headers necessários:
- `Content-Type: application/json`
- `x-company-id: 1`
- `Authorization: Bearer YOUR_TOKEN` (se autenticação estiver habilitada)

---

## 🚀 Como Testar

### Opção 1: Via IntelliJ IDEA

1. Abra o arquivo: `test-suspension.http`
2. Clique em "Run" ao lado da requisição
3. Verifique os logs da aplicação

### Opção 2: Via curl

```bash
# 1. Obter token (se necessário)
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "x-company-id: 1" \
  -d '{"username":"admin","password":"admin"}' \
  | jq -r '.token')

# 2. Executar suspensão manual
curl -X POST http://localhost:8080/api/test/trigger-suspension \
  -H "Content-Type: application/json" \
  -H "x-company-id: 1" \
  -H "Authorization: Bearer $TOKEN"
```

### Opção 3: Via Postman/Insomnia

1. **Method:** POST
2. **URL:** `http://localhost:8080/api/test/trigger-suspension`
3. **Headers:**
   - `Content-Type: application/json`
   - `x-company-id: 1`
   - `Authorization: Bearer YOUR_TOKEN`
4. **Send**

---

## 📊 Resposta Esperada

```
✅ Suspensão executada! Verifique os logs para detalhes.
```

---

## 🔍 Verificar Logs da Aplicação

Após executar o endpoint, verifique os logs no console do IntelliJ IDEA:

### ✅ Logs de Sucesso

```
🧪 TESTE MANUAL: Executando suspensão de contratos
==========================================================
SUSPENSÃO AUTOMÁTICA DE CONTRATOS POR INADIMPLÊNCIA
Data/Hora: 2026-01-27T10:05:30
==========================================================
----------------------------------------------------------
Processando empresa: Empresa Padrão (ID: 1)
Dias de tolerância configurados: 5 dias
----------------------------------------------------------
Data atual: 2026-01-27
Data limite para suspensão: 2026-01-22
Encontrados 2 contratos para suspensão
  🔒 Suspendendo contrato 4 - Cliente: Fernando Coelho
  ✅ Contrato 4 suspenso e bloqueado no Mikrotik com sucesso
  🔒 Suspendendo contrato 5 - Cliente: Maria Silva
  ✅ Contrato 5 suspenso e bloqueado no Mikrotik com sucesso
----------------------------------------------------------
Empresa Empresa Padrão: Resumo da suspensão automática
  • Contratos para processar: 2
  • ✅ Suspensos com sucesso: 2
  • ℹ️  Já estavam suspensos: 0
  • ❌ Erros: 0
----------------------------------------------------------
==========================================================
SUSPENSÃO AUTOMÁTICA CONCLUÍDA
==========================================================
```

### ℹ️ Se não houver contratos para suspender:

```
Encontrados 0 contratos para suspensão
```

Isso significa que:
- Não há faturas vencidas há 5+ dias
- Todos os contratos já estão suspensos
- As faturas não estão com status `OVERDUE`

---

## 🧪 Criar Cenário de Teste

Se não houver contratos para suspender, crie um cenário de teste:

### 1. Verificar faturas vencidas
```sql
SELECT 
    i.id,
    i.contract_id,
    i.due_date,
    i.status,
    DATEDIFF(CURRENT_DATE, i.due_date) AS days_overdue
FROM invoices i
WHERE i.company_id = 1
  AND i.status = 'OVERDUE'
ORDER BY i.due_date;
```

### 2. Forçar fatura vencida (para teste)
```sql
-- Criar fatura vencida há 7 dias
UPDATE invoices 
SET 
    due_date = DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY),
    status = 'OVERDUE'
WHERE id = YOUR_INVOICE_ID;
```

### 3. Garantir que o contrato está ativo
```sql
UPDATE contracts 
SET status = 'ACTIVE' 
WHERE id = YOUR_CONTRACT_ID;
```

### 4. Executar teste novamente
Execute o endpoint: `POST /api/test/trigger-suspension`

---

## ✅ Validar Resultado

### 1. Verificar status do contrato
```sql
SELECT id, status, updated_at 
FROM contracts 
WHERE id = YOUR_CONTRACT_ID;
```
**Esperado:** `status = 'SUSPENDED_FINANCIAL'`

### 2. Verificar PPPoE no banco
```sql
SELECT pu.id, pu.username, pu.status, pu.active
FROM pppoe_users pu
JOIN contracts c ON c.pppoe_user_id = pu.id
WHERE c.id = YOUR_CONTRACT_ID;
```
**Esperado:** `status = 'DISABLED'`

### 3. Verificar perfil no Mikrotik (SSH)
```bash
/ppp secret print where name="USERNAME_DO_CLIENTE"
```
**Esperado:** `profile: BLOQUEADO`

### 4. Verificar se foi desconectado
```bash
/ppp active print where name="USERNAME_DO_CLIENTE"
```
**Esperado:** Nenhum resultado (desconectado)

---

## 🔧 Troubleshooting

### ❌ Erro 404 - Endpoint não encontrado

**Solução:** Reinicie a aplicação Spring Boot para registrar o novo endpoint.

### ❌ Erro 401 - Unauthorized

**Solução:** 
1. Obtenha um token válido via `/api/auth/login`
2. Adicione no header: `Authorization: Bearer TOKEN`

### ❌ Nenhum contrato encontrado para suspensão

**Causas possíveis:**
1. Não há faturas vencidas há 5+ dias
2. Faturas não estão com status `OVERDUE`
3. Contratos não estão `ACTIVE`

**Solução:** Crie um cenário de teste (ver seção acima)

### ❌ Erro ao bloquear no Mikrotik

**Verificar:**
1. Perfil "BLOQUEADO" existe no Mikrotik?
2. Credenciais SSH estão corretas?
3. Usuário PPPoE está vinculado ao contrato?

---

## 📅 Execução Automática

Lembre-se: O job roda **automaticamente todos os dias às 03:00 AM**.

```java
@Scheduled(cron = "0 0 3 * * ?") // Todo dia às 03:00
public void suspendOverdueContracts()
```

O endpoint de teste é apenas para **validação manual**.

---

## ⚠️ IMPORTANTE

- Este endpoint é **temporário** para testes
- Remova antes de ir para produção (opcional)
- Sempre verifique os logs após executar
- Teste primeiro em ambiente de desenvolvimento

---

## 📝 Resumo do Fluxo

1. **Executa endpoint** → `/api/test/trigger-suspension`
2. **Busca faturas** vencidas há ≥ 5 dias
3. **Filtra contratos** ACTIVE com faturas OVERDUE
4. **Para cada contrato:**
   - Altera perfil → "BLOQUEADO" (Mikrotik)
   - Desconecta usuário ativo (Mikrotik)
   - Atualiza status → SUSPENDED_FINANCIAL (banco)
   - Atualiza PPPoE → DISABLED (banco)
5. **Retorna resumo** nos logs

---

**Data:** 2026-01-27  
**Arquivo:** `InvoiceBillingJob.java`  
**Endpoint:** `POST /api/test/trigger-suspension`  
**Status:** ✅ Implementado e pronto para testes
