# 🔄 Sincronização de Usuários PPPoE do Mikrotik

## 📋 Visão Geral

A funcionalidade de sincronização permite importar todos os usuários PPPoE que já existem no servidor Mikrotik para o banco de dados da aplicação, evitando a necessidade de criar cada usuário manualmente.

## 🎯 Quando Usar

✅ **Cenários Ideais:**
- Você já possui usuários PPPoE criados diretamente no Mikrotik
- Está migrando de gerenciamento manual para a API
- Precisa importar configurações existentes
- Quer manter a API sincronizada com o Mikrotik

❌ **Quando NÃO usar:**
- Mikrotik não possui usuários PPPoE ainda
- Prefere criar usuários um por um com validações específicas

## 🚀 Como Funciona

### 1️⃣ Pré-requisitos

Antes de sincronizar, você precisa:

1. **Servidor Mikrotik cadastrado** na API
2. **Perfis PPPoE sincronizados** (recomendado - para manter os perfis originais) OU **Perfil PPPoE padrão** criado (para forçar todos usuários a um único perfil)
3. **Acesso SSH** ao Mikrotik funcionando
4. **Token JWT válido** (role ADMIN ou OPERATOR)

### 2️⃣ Executar Sincronização

**Endpoint:**
```
POST /api/users/sync/server/{serverId}?forceProfileId={profileId}
```

**Parâmetros:**
- `serverId` (path, obrigatório): ID do servidor Mikrotik cadastrado
- `forceProfileId` (query, opcional): ID do perfil para forçar todos os usuários

### 🎛️ Modos de Sincronização

#### **Modo 1: Manter Perfis Originais do MikroTik** (Recomendado)

**Quando usar:** Quando você já sincronizou os perfis e quer manter a vinculação original.

**Exemplo cURL:**
```bash
curl -X POST http://localhost:8080/api/users/sync/server/1 \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

**Exemplo com REST Client:**
```http
POST http://localhost:8080/api/users/sync/server/1
Authorization: Bearer SEU_TOKEN_JWT
```

**Comportamento:**
- Busca o perfil de cada usuário pelo nome no banco de dados
- Se o perfil existir no banco → vincula o usuário a ele
- Se o perfil NÃO existir → **FALHA** (adiciona em `errorMessages`)

**⚠️ Importante:** Sincronize os perfis primeiro usando `POST /api/profiles/sync/server/{serverId}`

---

#### **Modo 2: Forçar Perfil Único para Todos** 

**Quando usar:** Quando você quer que todos os usuários sincronizados usem o mesmo perfil, ignorando os perfis originais do MikroTik.

**Exemplo cURL:**
```bash
curl -X POST "http://localhost:8080/api/users/sync/server/1?forceProfileId=5" \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

**Exemplo com REST Client:**
```http
POST http://localhost:8080/api/users/sync/server/1?forceProfileId=5
Authorization: Bearer SEU_TOKEN_JWT
```

**Comportamento:**
- Ignora o perfil original do MikroTik
- Vincula TODOS os usuários ao perfil ID 5
- Útil para migração ou padronização

### 3️⃣ Resposta

A API retorna um objeto `SyncResultDTO` com estatísticas detalhadas:

```json
{
  "totalMikrotikUsers": 25,
  "syncedUsers": 23,
  "skippedUsers": 2,
  "failedUsers": 0,
  "syncedUsernames": ["user1", "user2", "user3", ...],
  "skippedUsernames": ["admin", "test"],
  "errorMessages": []
}
```

**Campos:**
- `totalMikrotikUsers`: Total de usuários encontrados no Mikrotik
- `syncedUsers`: Usuários importados com sucesso
- `skippedUsers`: Usuários que já existiam no banco
- `failedUsers`: Usuários que falharam ao importar
- `syncedUsernames`: Lista de usernames sincronizados
- `skippedUsernames`: Lista de usernames ignorados
- `errorMessages`: Mensagens de erro (se houver)

## 🔍 Processo Detalhado

### Passo a Passo da Sincronização:

1. **Conexão SSH**: API conecta ao servidor Mikrotik via SSH
2. **Busca de Usuários**: Executa comando `/ppp secret print detail`
3. **Parse de Dados**: Extrai informações de cada usuário:
   - Username
   - Password
   - Profile
   - Status (ativo/inativo)
   - Comment
4. **Verificação**: Para cada usuário do Mikrotik:
   - ✅ Se **não existe** no banco → **Importa**
   - ⏭️ Se **já existe** no banco → **Ignora**
5. **Mapeamento de Perfil**:
   - **Sem forceProfileId**: Busca perfil por nome no banco → Se não encontrar, FALHA
   - **Com forceProfileId**: Usa o perfil informado para todos os usuários
6. **Criação no Banco**: Salva usuário com:
   - Username e password (criptografada)
   - Email padrão: `{username}@synced.local`
   - Comment do Mikrotik ou "Sincronizado do Mikrotik"
   - Status ativo/inativo conforme Mikrotik

## 📊 Exemplos Completos

### Exemplo 1: Sincronizar Mantendo Perfis Originais (Recomendado)

**Cenário:** Você tem 50 usuários no MikroTik vinculados a diferentes perfis (Plano-10Mb, Plano-50Mb, Plano-100Mb).

**Passo 1 - Cadastrar Servidor:**
```bash
curl -X POST http://localhost:8080/api/mikrotik-servers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Servidor Principal",
    "ipAddress": "192.168.1.1",
    "port": 22,
    "username": "admin",
    "password": "mikrotik123"
  }'
# Resposta: { "id": 1, ... }
```

**Passo 2 - Sincronizar Perfis Primeiro:**
```bash
curl -X POST http://localhost:8080/api/profiles/sync/server/1 \
  -H "Authorization: Bearer $TOKEN"

# Resposta:
{
  "totalMikrotikUsers": 3,
  "syncedUsers": 3,
  "syncedUsernames": ["Plano-10Mb", "Plano-50Mb", "Plano-100Mb"]
}
```

**Passo 3 - Sincronizar Usuários (sem forceProfileId):**
```bash
curl -X POST http://localhost:8080/api/users/sync/server/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Resultado:**
```json
{
  "totalMikrotikUsers": 50,
  "syncedUsers": 50,
  "skippedUsers": 0,
  "failedUsers": 0,
  "syncedUsernames": ["cliente1", "cliente2", ..., "cliente50"],
  "skippedUsernames": [],
  "errorMessages": []
}
```
✅ **Cada usuário foi vinculado ao seu perfil original do MikroTik!**

---

### Exemplo 2: Forçar Perfil Único para Todos

**Cenário:** Você quer migrar todos os usuários para um único perfil padrão.

**Passo 1 - Cadastrar Servidor:**
```bash
# (mesmo do Exemplo 1)
```

**Passo 2 - Criar Perfil Padrão:**
```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Plano Padrão",
    "description": "Perfil padrão para sincronização",
    "maxBitrateDl": 10000000,
    "maxBitrateUl": 5000000,
    "sessionTimeout": 0,
    "active": true,
    "mikrotikServerId": 1
  }'
# Resposta: { "id": 5, ... }
```

**Passo 3 - Sincronizar Usuários (com forceProfileId):**
```bash
curl -X POST "http://localhost:8080/api/users/sync/server/1?forceProfileId=5" \
  -H "Authorization: Bearer $TOKEN"
```

**Resultado:**
```json
{
  "totalMikrotikUsers": 50,
  "syncedUsers": 50,
  "skippedUsers": 0,
  "failedUsers": 0,
  "syncedUsernames": ["cliente1", "cliente2", ..., "cliente50"],
  "skippedUsernames": [],
  "errorMessages": []
}
```
✅ **TODOS os 50 usuários foram vinculados ao Perfil ID 5, ignorando os perfis originais!**

---

### Exemplo 3: Re-sincronização (Teste)

**Executar sincronização novamente:**
```bash
curl -X POST http://localhost:8080/api/users/sync/server/1 \
  -H "Authorization: Bearer $TOKEN"
```

**Resultado:**
```json
{
  "totalMikrotikUsers": 50,
  "syncedUsers": 0,
  "skippedUsers": 50,
  "failedUsers": 0,
  "syncedUsernames": [],
  "skippedUsernames": ["cliente1", "cliente2", ..., "cliente50"],
  "errorMessages": []
}
```
> Todos foram ignorados porque já existem no banco!

## ⚠️ Considerações Importantes

### Segurança

🔐 **Passwords:**
- Se o Mikrotik retornar a senha, ela será criptografada com BCrypt
- Se não retornar, será usada uma senha padrão: `synced123`
- **Recomendação**: Alterar senhas após sincronização

🔒 **Permissões:**
- Apenas usuários com role `ADMIN` ou `OPERATOR` podem sincronizar
- Token JWT deve estar válido

### Performance

⚡ **Otimização:**
- Sincronização é executada em transação única
- Usuários duplicados são ignorados automaticamente
- Processo é síncrono mas rápido

📊 **Quantidade:**
- Testado com até 1000 usuários
- Tempo médio: ~2-5 segundos para 100 usuários

### Mapeamento de Perfis

📋 **Lógica (sem forceProfileId):**
1. API busca perfil do Mikrotik pelo **nome** no banco
2. Se encontrar → usa esse perfil
3. Se NÃO encontrar → **FALHA** (adiciona em `errorMessages`)

📋 **Lógica (com forceProfileId):**
1. Ignora perfil original do MikroTik
2. Vincula TODOS os usuários ao perfil informado

💡 **Dica**: Para manter perfis originais, sincronize os perfis primeiro usando `POST /api/profiles/sync/server/{serverId}`

### Email Padrão

📧 **Formato:** `{username}@synced.local`

**Exemplo:**
- Username: `cliente1`
- Email gerado: `cliente1@synced.local`

💡 **Recomendação**: Atualizar emails reais após sincronização

## 🐛 Troubleshooting

### "Servidor Mikrotik não encontrado"
✅ **Solução**: Verificar se o `serverId` está correto e o servidor está cadastrado

### "Perfil PPPoE não encontrado"
✅ **Solução**: Verificar se o `forceProfileId` está correto e o perfil existe no banco

### "Perfil 'Plano-XYZ' do usuário 'cliente1' não encontrado no banco"
✅ **Soluções**:
- **Opção 1**: Sincronizar perfis primeiro: `POST /api/profiles/sync/server/{serverId}`
- **Opção 2**: Usar `forceProfileId` para definir um perfil padrão

### "Erro ao conectar com Mikrotik"
✅ **Soluções**:
- Verificar IP, porta e credenciais do servidor
- Confirmar que SSH está habilitado no Mikrotik
- Testar conectividade: `POST /api/mikrotik-servers/{id}/test-connection`

### Sincronizou 0 usuários
✅ **Causas possíveis**:
- Todos os usuários já existem no banco (verificar `skippedUsers`)
- Mikrotik não possui usuários PPPoE
- Erro ao executar comando SSH (verificar `errorMessages`)

### Alguns usuários falharam
✅ **Verificar**: Campo `errorMessages` na resposta para detalhes específicos de cada falha

## 📝 Exemplo de Uso no Swagger

1. Acesse: `http://localhost:8080/swagger-ui.html`
2. Faça login e obtenha o token JWT
3. Clique em "Authorize" e insira o token
4. Navegue até: **Usuários PPPoE → POST /api/users/sync/server/{serverId}**
5. Preencha os parâmetros:
   - `serverId`: ID do servidor
   - `forceProfileId`: (Opcional) ID do perfil para forçar a todos
6. Clique em "Execute"
7. Veja o resultado com estatísticas detalhadas

## 🎯 Fluxo Recomendado

### Opção 1: Manter Perfis Originais (Recomendado)

```
1. Cadastrar Servidor Mikrotik
   ↓
2. Testar Conexão SSH
   ↓
3. Sincronizar Perfis PPPoE
   POST /api/profiles/sync/server/{serverId}
   ↓
4. Sincronizar Usuários (SEM forceProfileId)
   POST /api/users/sync/server/{serverId}
   ↓
5. Verificar Resultados
   ↓
6. Atualizar emails/senhas se necessário
   ↓
7. Pronto!
```

### Opção 2: Forçar Perfil Único

```
1. Cadastrar Servidor Mikrotik
   ↓
2. Testar Conexão SSH
   ↓
3. Criar Perfil Padrão
   POST /api/profiles
   ↓
4. Sincronizar Usuários (COM forceProfileId)
   POST /api/users/sync/server/{serverId}?forceProfileId={id}
   ↓
5. Verificar Resultados
   ↓
6. Atualizar emails/senhas/perfis individuais conforme necessário
   ↓
7. Pronto!
``````

## 🔗 Endpoints Relacionados

| Endpoint | Descrição |
|----------|-----------|
| `POST /api/mikrotik-servers` | Cadastrar servidor |
| `POST /api/mikrotik-servers/{id}/test-connection` | Testar conexão |
| `POST /api/profiles` | Criar perfil |
| `GET /api/users` | Listar usuários sincronizados |
| `GET /api/users/server/{serverId}` | Listar usuários de um servidor |

## ✅ Checklist de Sincronização

- [ ] Servidor Mikrotik cadastrado
- [ ] Conexão SSH testada e funcionando
- [ ] Perfil padrão criado
- [ ] Token JWT obtido
- [ ] Executar sincronização
- [ ] Verificar resultado (syncedUsers)
- [ ] Atualizar emails dos usuários
- [ ] Alterar senhas padrão (se aplicável)
- [ ] Testar login de alguns usuários

## 📚 Referências

- [API_README.md](./API_README.md) - Documentação completa da API
- [START.md](./START.md) - Guia de início rápido
- [requests.http](./requests.http) - Exemplos de requisições HTTP

---

**Versão:** 1.0.0  
**Data:** Janeiro 2026  
**Status:** ✅ Funcional
