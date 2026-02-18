# 🚀 Sincronização Completa Automática - MikroTik para Sistema

## 📋 Visão Geral

Sistema completo de sincronização automática que resolve o problema de migração de provedores com **centenas de clientes** já existentes no MikroTik.

### 🎯 Problema Resolvido

**ANTES**: Provedor com 500 clientes no MikroTik precisava:
- ✅ Sincronizar 500 usuários PPPoE
- ❌ Criar manualmente 500 cadastros de clientes
- ❌ Criar manualmente 500 contratos
- ⏰ **Tempo estimado**: Dias ou semanas de trabalho manual

**AGORA**: Com a sincronização completa:
- ✅ Sincroniza PPPoE automaticamente
- ✅ Cria clientes automaticamente (parseando comentários)
- ✅ Cria planos de serviço automaticamente
- ✅ Cria e ativa contratos automaticamente
- ⏰ **Tempo**: Minutos (dependendo da quantidade)

---

## 🏗️ Arquitetura da Solução

### Fluxo das 5 Fases

```
┌─────────────────────────────────────────────────────────────┐
│                   SINCRONIZAÇÃO COMPLETA                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 1: Sincronizar Profiles PPPoE do MikroTik             │
│  ➜ Importa todos os profiles técnicos (ex: PLANO-40M)       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 2: Criar Planos de Serviço                            │
│  ➜ Para cada profile, cria um ServicePlan                   │
│  ➜ Nome: "Plano PLANO-40M"                                  │
│  ➜ Preço: R$ 50,00 (configurável)                          │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 3: Sincronizar Usuários PPPoE                         │
│  ➜ Importa todos os usuários do MikroTik                    │
│  ➜ Mantém comentários originais                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 4: Criar Clientes (Parsing Inteligente)               │
│  ➜ Extrai nome do comentário ou usa username                │
│  ➜ Identifica endereço (rua, número, bairro)               │
│  ➜ Cria endereço de instalação automaticamente              │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  FASE 5: Criar e Ativar Contratos                           │
│  ➜ Vincula Cliente + Plano + PPPoE                         │
│  ➜ Ativa automaticamente (opcional)                         │
│  ➜ Define dia de vencimento padrão                          │
└─────────────────────────────────────────────────────────────┘
```

---

## 🧠 Parsing Inteligente de Comentários

### Exemplos Reais do MikroTik

| Comentário Original | Nome Extraído | Endereço | Número |
|---------------------|---------------|----------|--------|
| `felipe achy/ nalmar alcantara n255` | Felipe Achy | Nalmar Alcantara | 255 |
| `rua7 n128` | _username_ | Rua7 | 128 |
| `travessa o mangabeira n214` | _username_ | Travessa O Mangabeira | 214 |
| `rua A n310` | _username_ | Rua A | 310 |
| _vazio_ | _username_ | - | - |

### Algoritmo de Parsing

```java
// 1. Nome do cliente: primeira parte antes de "/" ou "n<número>"
Pattern: "^([^/n]+?)(?:/|n\\d|$)"

// 2. Endereço: rua, travessa, avenida + texto
Pattern: "(rua|travessa|avenida|av|trav)\\s+([^/n]+)"

// 3. Número: "n" seguido de dígitos
Pattern: "\\bn\\s*(\\d+)"

// 4. Fallback: Se não extrair nome, usa username capitalizado
```

---

## 📡 Endpoint da API

### **POST** `/api/sync/full-sync`

#### Permissão
- **Role**: `ADMIN` (apenas administradores)

#### Request Body

```json
{
  "serverId": 1,
  "defaultBillingDay": 10,
  "defaultPlanPrice": 50.00,
  "createMissingServicePlans": true,
  "createMissingCustomers": true,
  "createContracts": true,
  "autoActivateContracts": true
}
```

#### Parâmetros

| Campo | Tipo | Obrigatório | Padrão | Descrição |
|-------|------|-------------|--------|-----------|
| `serverId` | Long | ✅ Sim | - | ID do servidor MikroTik |
| `defaultBillingDay` | Integer | ❌ Não | 10 | Dia de vencimento (1-28) |
| `defaultPlanPrice` | BigDecimal | ❌ Não | 50.00 | Preço padrão para planos criados |
| `createMissingServicePlans` | Boolean | ❌ Não | true | Criar planos automaticamente |
| `createMissingCustomers` | Boolean | ❌ Não | true | Criar clientes automaticamente |
| `createContracts` | Boolean | ❌ Não | true | Criar contratos automaticamente |
| `autoActivateContracts` | Boolean | ❌ Não | true | Ativar contratos automaticamente |

#### Response

```json
{
  "totalProfiles": 15,
  "syncedProfiles": 15,
  "skippedProfiles": 0,
  "createdServicePlans": 15,
  "existingServicePlans": 0,
  "totalPppoeUsers": 500,
  "syncedPppoeUsers": 500,
  "skippedPppoeUsers": 0,
  "createdCustomers": 485,
  "existingCustomers": 15,
  "createdContracts": 500,
  "activatedContracts": 500,
  "failedContracts": 0,
  "syncedProfileNames": ["PLANO-40M", "PLANO-100M", ...],
  "createdServicePlanNames": ["Plano PLANO-40M", ...],
  "syncedPppoeUsernames": ["junior", "tinaa", ...],
  "createdCustomerNames": ["Junior", "Tina", ...],
  "createdContractIds": ["1", "2", "3", ...],
  "errorMessages": [],
  "warnings": [
    "PPPoE junior: sem comentário, usando username como nome"
  ],
  "executionTimeSeconds": 45,
  "success": true
}
```

---

## 🔄 Lógica de Duplicação

### Como o Sistema Evita Duplicatas

1. **Profiles**: Verifica por nome no banco antes de criar
2. **Planos**: Verifica se já existe plano para o profile
3. **Usuários PPPoE**: Verifica por username + servidor
4. **Clientes**: Verifica por nome + empresa
5. **Contratos**: Verifica se já existe contrato para o PPPoE

### Marcação de Relacionamentos

O sistema marca o comentário do PPPoE com o ID do cliente criado:

```
Comentário original: "felipe achy/ nalmar alcantara n255"
Após criação:        "felipe achy/ nalmar alcantara n255 [CUSTOMER_ID:123]"
```

Isso permite:
- Evitar duplicação na próxima sincronização
- Rastrear qual cliente foi criado para qual PPPoE

---

## 📊 Exemplo de Uso Real

### Cenário: Provedor com 500 Clientes

```bash
# Request
POST http://localhost:8080/api/sync/full-sync
Authorization: Bearer {token}
Content-Type: application/json

{
  "serverId": 1,
  "defaultBillingDay": 10,
  "defaultPlanPrice": 50.00,
  "createMissingServicePlans": true,
  "createMissingCustomers": true,
  "createContracts": true,
  "autoActivateContracts": true
}
```

### Resultado Esperado

```
==========================================================
>>> SINCRONIZAÇÃO COMPLETA FINALIZADA <<<
Tempo: 45s | Sucesso: true
Profiles: 15/15 sincronizados
Planos criados: 15
PPPoE sincronizados: 500/500
Clientes criados: 485
Contratos criados: 500
Contratos ativados: 500
Erros: 0
Avisos: 15
==========================================================
```

**Resultado**:
- ✅ 15 Profiles importados
- ✅ 15 Planos de serviço criados automaticamente
- ✅ 500 Usuários PPPoE importados
- ✅ 485 Clientes novos criados (15 já existiam)
- ✅ 500 Contratos criados e ativados
- ⏰ **Tempo total**: 45 segundos

---

## ⚠️ Avisos e Validações

### Avisos Comuns

```
"PPPoE junior: sem comentário, usando username como nome"
"Cliente já existe com nome: Fernando Costa"
"Plano de serviço não encontrado para profile: BLOQUEADO"
"Cliente não encontrado para PPPoE: testuser"
```

### Erros que Podem Ocorrer

1. **Servidor não encontrado**: Verificar se `serverId` é válido
2. **Profile sem plano**: Ativar `createMissingServicePlans: true`
3. **Comentário inválido**: Sistema usa username como fallback
4. **Falha na ativação**: Contrato criado mas não ativado (verificar logs)

---

## 🔧 Configurações Recomendadas

### Para Primeira Sincronização

```json
{
  "serverId": 1,
  "defaultBillingDay": 10,
  "defaultPlanPrice": 50.00,
  "createMissingServicePlans": true,  // ✅ Criar planos
  "createMissingCustomers": true,     // ✅ Criar clientes
  "createContracts": true,            // ✅ Criar contratos
  "autoActivateContracts": true       // ✅ Ativar automaticamente
}
```

### Para Sincronização Incremental (Apenas Novos)

```json
{
  "serverId": 1,
  "defaultBillingDay": 10,
  "defaultPlanPrice": 50.00,
  "createMissingServicePlans": false,  // ❌ Planos já existem
  "createMissingCustomers": true,      // ✅ Criar novos clientes
  "createContracts": true,             // ✅ Criar contratos
  "autoActivateContracts": true        // ✅ Ativar automaticamente
}
```

---

## 📝 Dados Criados Automaticamente

### Cliente

```java
Customer {
  name: "Fernando Costa",              // Parseado do comentário
  type: FISICA,                         // Padrão
  document: "000.000.000-00",          // Placeholder (atualizar depois)
  email: "fernandoco@pendente.com",    // Gerado do username
  phonePrimary: null,                  // Extrair se existir no comentário
  status: ACTIVE,
  notes: "Cliente criado automaticamente na sincronização..."
}
```

### Endereço

```java
Address {
  street: "Rua Dois De Julho",         // Parseado do comentário
  number: "35",                         // Parseado do comentário
  district: null,                      // Não parseado ainda
  city: "A definir",                   // Placeholder
  state: "BA",                         // Padrão
  zipCode: "00000-000",                // Placeholder
  type: INSTALLATION
}
```

### Contrato

```java
Contract {
  customerId: 123,                     // Cliente criado
  servicePlanId: 5,                    // Plano do profile
  pppoeUserId: 369,                    // Usuário PPPoE sincronizado
  installationAddressId: 456,          // Endereço criado
  status: ACTIVE,                      // Ativado automaticamente
  billingDay: 10,                      // Configurado
  amount: 50.00,                       // Do plano de serviço
  startDate: "2026-02-12"              // Data da sincronização
}
```

---

## 🚨 Importante

### Antes de Executar

1. **Backup do Banco**: Sempre fazer backup antes da primeira sincronização
2. **Horário**: Executar em horário de baixo movimento
3. **Teste**: Testar primeiro com um servidor de homologação
4. **Verificação**: Conferir alguns clientes criados manualmente após sincronização

### Após Executar

1. **Atualizar Documentos**: CPF/CNPJ dos clientes criados
2. **Verificar Endereços**: Cidade, CEP e complementos
3. **Ajustar Preços**: Se necessário, alterar valores de planos
4. **Revisar Avisos**: Verificar warnings no resultado da sincronização

---

## 📂 Arquivos da Implementação

### DTOs
- `FullSyncConfigDTO.java` - Configuração da sincronização
- `FullSyncResultDTO.java` - Resultado detalhado
- `CustomerInfoParseResult.java` - Dados parseados do comentário

### Services
- `FullSyncService.java` - Orquestrador principal (5 fases)

### Controllers
- `FullSyncController.java` - Endpoint REST

### Repositories (métodos adicionados)
- `CustomerRepository.findByNameAndCompanyId()`
- `ServicePlanRepository.findByPppoeProfileIdAndCompanyId()`
- `ContractRepository.findByPppoeUserIdAndCompanyId()`

---

## 🎉 Benefícios

1. **Economia de Tempo**: De dias para minutos
2. **Redução de Erros**: Automação elimina erros manuais
3. **Rastreabilidade**: Logs completos de tudo que foi criado
4. **Flexibilidade**: Configurações granulares para cada etapa
5. **Segurança**: Não duplica dados existentes
6. **Inteligência**: Parsing automático de comentários

---

## 🔮 Melhorias Futuras

- [ ] IA para melhorar parsing de endereços
- [ ] Integração com ViaCEP para completar endereços
- [ ] Detecção de CPF/CNPJ em comentários
- [ ] Parsing de telefones em comentários
- [ ] Relatório PDF após sincronização
- [ ] Notificação por email quando concluir
- [ ] Rollback automático em caso de falha

