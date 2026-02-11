# 🔧 SOLUÇÃO: Endpoint de Teste com Erro 403

## ❌ Problema Identificado

O endpoint `/api/test/trigger-suspension` estava retornando **HTTP 403 Forbidden** porque o Spring Security estava bloqueando requisições sem autenticação.

## ✅ Solução Implementada

### 1. **SecurityConfig.java - Adicionada Permissão**

```java
.requestMatchers("/api/test/**").permitAll() // ← ADICIONADO
```

Agora o endpoint de teste **NÃO requer autenticação**.

---

## 🚀 TESTE AGORA (Após Reiniciar)

### ⚠️ **IMPORTANTE: REINICIE A APLICAÇÃO SPRING BOOT**

As mudanças no `SecurityConfig` só entram em vigor após reiniciar.

**No IntelliJ IDEA:**
1. Pare a aplicação (Stop)
2. Inicie novamente (Run)
3. Aguarde: `Started MikrotikApplication in X seconds`

---

### Teste 1: Via HTTP Client (IntelliJ)

1. Abra: `test-suspension.http`
2. Clique em **▶ Run** ao lado da requisição
3. Deve retornar: `✅ Suspensão executada! Verifique os logs para detalhes.`

### Teste 2: Via curl (Terminal)

```bash
curl -X POST http://localhost:8080/api/test/trigger-suspension \
  -H "Content-Type: application/json" \
  -H "x-company-id: 1"
```

**Resposta esperada:**
```
✅ Suspensão executada! Verifique os logs para detalhes.
```

---

## 🔍 DIAGNÓSTICO: Por que não suspendeu?

Execute as queries no arquivo: `test-suspension-queries.sql`

### Query 1: Verificar se há faturas elegíveis

```sql
SELECT 
    i.id AS invoice_id,
    i.contract_id,
    i.due_date,
    DATEDIFF(CURRENT_DATE, i.due_date) AS days_overdue,
    i.status AS invoice_status,
    c.status AS contract_status
FROM invoices i
JOIN contracts c ON c.id = i.contract_id
WHERE i.company_id = 1
  AND i.status = 'OVERDUE'
  AND i.due_date <= DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY)
  AND c.status = 'ACTIVE';
```

**Se retornar 0 linhas:**
- ✅ Não há contratos elegíveis para suspensão
- ℹ️ Sistema está funcionando corretamente
- 🛠️ Você precisa criar um cenário de teste

**Se retornar linhas:**
- ✅ Há contratos para suspender
- ❌ Algo está impedindo a suspensão
- 🔍 Verifique os logs para identificar o erro

---

## 🛠️ CRIAR CENÁRIO DE TESTE

### Passo 1: Encontrar uma fatura

```sql
SELECT id, contract_id, due_date, status 
FROM invoices 
WHERE company_id = 1 
LIMIT 5;
```

### Passo 2: Forçar fatura vencida há 7 dias

```sql
UPDATE invoices 
SET 
    due_date = DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY),
    status = 'OVERDUE'
WHERE id = 1; -- ← Substitua pelo ID da sua fatura
```

### Passo 3: Garantir que o contrato está ACTIVE

```sql
UPDATE contracts 
SET status = 'ACTIVE' 
WHERE id = 1; -- ← ID do contrato da fatura
```

### Passo 4: Verificar se há pppoe_user_id

```sql
SELECT 
    c.id,
    c.pppoe_user_id,
    pu.username
FROM contracts c
LEFT JOIN pppoe_users pu ON pu.id = c.pppoe_user_id
WHERE c.id = 1;
```

**Se `pppoe_user_id` for NULL:**

```sql
-- Vincular um usuário PPPoE ao contrato
UPDATE contracts 
SET pppoe_user_id = (
    SELECT id FROM pppoe_users 
    WHERE company_id = 1 
    LIMIT 1
)
WHERE id = 1;
```

### Passo 5: Executar teste novamente

```bash
POST http://localhost:8080/api/test/trigger-suspension
```

---

## 📊 ANALISAR LOGS

Após executar o teste, verifique os logs no **console do IntelliJ IDEA**:

### ✅ Cenário 1: Sem contratos para suspender

```
🧪 TESTE MANUAL: Executando suspensão de contratos
==========================================================
SUSPENSÃO AUTOMÁTICA DE CONTRATOS POR INADIMPLÊNCIA
...
Processando empresa: Empresa Padrão (ID: 1)
Dias de tolerância configurados: 5 dias
Data atual: 2026-01-27
Data limite para suspensão: 2026-01-22
Encontrados 0 contratos para suspensão  ← AQUI!
...
```

**Interpretação:**
- ✅ Sistema funcionando
- ℹ️ Não há faturas vencidas há 5+ dias
- 🛠️ Crie um cenário de teste

---

### ✅ Cenário 2: Contratos suspensos com sucesso

```
Encontrados 2 contratos para suspensão
  🔒 Suspendendo contrato 4 - Cliente: Fernando
  ✅ Contrato 4 suspenso e bloqueado no Mikrotik com sucesso
  🔒 Suspendendo contrato 5 - Cliente: Maria
  ✅ Contrato 5 suspenso e bloqueado no Mikrotik com sucesso
----------------------------------------------------------
  • ✅ Suspensos com sucesso: 2
  • ℹ️  Já estavam suspensos: 0
  • ❌ Erros: 0
```

**Validar:**
```sql
SELECT status FROM contracts WHERE id = 4; -- SUSPENDED_FINANCIAL
SELECT status FROM pppoe_users WHERE id = (SELECT pppoe_user_id FROM contracts WHERE id = 4); -- DISABLED
```

---

### ❌ Cenário 3: Erro ao suspender

```
  ❌ Erro ao suspender contrato 4: Credencial PPPoE não encontrada
```

**Solução:**
```sql
-- Verificar se contrato tem pppoe_user_id
SELECT pppoe_user_id FROM contracts WHERE id = 4;

-- Se NULL, vincular:
UPDATE contracts 
SET pppoe_user_id = (SELECT id FROM pppoe_users WHERE username = 'usuario_do_cliente')
WHERE id = 4;
```

---

### ❌ Cenário 4: Erro no Mikrotik

```
❌ COMANDO RETORNOU EXIT STATUS: 1
Resultado do comando: input does not match any value of profile
```

**Causa:** Perfil "BLOQUEADO" não existe no Mikrotik

**Solução:**
1. **Criar no Mikrotik (SSH):**
```bash
/ppp profile add name=BLOQUEADO rate-limit=64k/64k local-address=10.0.0.1
```

2. **Sincronizar profiles:**
```bash
POST http://localhost:8080/api/pppoe-profiles/sync/server/1
```

3. **Executar teste novamente**

---

## 📝 CHECKLIST DE VERIFICAÇÃO

Antes de executar o teste, certifique-se:

- [ ] ✅ Aplicação Spring Boot foi **REINICIADA**
- [ ] ✅ Há faturas com status `OVERDUE`
- [ ] ✅ Faturas estão vencidas há **5+ dias**
- [ ] ✅ Contratos estão com status `ACTIVE`
- [ ] ✅ Contratos têm `pppoe_user_id` vinculado
- [ ] ✅ Perfil "BLOQUEADO" existe no Mikrotik
- [ ] ✅ Usuário PPPoE está ativo no banco

---

## 🎯 RESUMO DO FLUXO

```
1. Você executa: POST /api/test/trigger-suspension
                     ↓
2. SecurityConfig: ✅ Permite (sem autenticação)
                     ↓
3. InvoiceBillingJob.triggerSuspensionManually()
                     ↓
4. Busca empresas ativas
                     ↓
5. Para cada empresa:
   ├─ Define CompanyContextHolder
   ├─ Busca faturas OVERDUE vencidas há ≥ 5 dias
   ├─ Filtra contratos ACTIVE
   └─ Suspende cada contrato:
      ├─ Altera perfil → BLOQUEADO (Mikrotik)
      ├─ Desconecta usuário (Mikrotik)
      ├─ Atualiza status → SUSPENDED_FINANCIAL (banco)
      └─ Atualiza PPPoE → DISABLED (banco)
                     ↓
6. Retorna: "✅ Suspensão executada!"
                     ↓
7. Verifique os logs para detalhes
```

---

## 📞 AINDA NÃO FUNCIONOU?

### Verifique:

1. **Aplicação foi reiniciada?**
   - Mudanças no `SecurityConfig` requerem restart

2. **Endpoint retorna 403?**
   - Confirme que a linha `.requestMatchers("/api/test/**").permitAll()` está no `SecurityConfig`
   - Reinicie novamente

3. **Endpoint retorna 200 mas não suspende?**
   - Execute as queries de diagnóstico (`test-suspension-queries.sql`)
   - Verifique se há contratos elegíveis
   - Analise os logs para identificar o problema

4. **Logs não aparecem?**
   - Verifique o console do IntelliJ IDEA
   - Procure por: `🧪 TESTE MANUAL: Executando suspensão de contratos`

---

## 📁 Arquivos Criados/Modificados

✅ `SecurityConfig.java` - Adicionada permissão para `/api/test/**`
✅ `test-suspension.http` - Atualizado (sem token)
✅ `test-suspension-queries.sql` - Queries de diagnóstico
✅ `TROUBLESHOOTING_SUSPENSION.md` - Este arquivo

---

**Data:** 2026-01-27  
**Status:** ✅ **Corrigido - Aguardando reinício da aplicação**  
**Próximo passo:** **REINICIE** a aplicação e teste novamente
