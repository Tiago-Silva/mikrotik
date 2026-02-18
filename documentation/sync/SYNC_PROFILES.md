# Sincronização de Perfis PPPoE

## Visão Geral

A funcionalidade de sincronização de perfis permite importar perfis PPPoE existentes no servidor MikroTik para o banco de dados da aplicação, evitando a necessidade de criar cada perfil manualmente.

## Quando Usar

- **Servidor novo com perfis já configurados**: Quando você está integrando um servidor MikroTik que já possui perfis PPPoE configurados
- **Migração**: Ao migrar de outra solução para este sistema
- **Backup/Recuperação**: Para sincronizar perfis após uma restauração de configuração no MikroTik

## Endpoint

```
POST /api/profiles/sync/server/{serverId}
```

### Parâmetros

- `serverId` (path): ID do servidor MikroTik cadastrado no sistema

### Autenticação

Requer token JWT com perfil ADMIN ou OPERATOR.

### Headers

```
Authorization: Bearer {token}
Content-Type: application/json
```

## Funcionamento

1. **Conexão**: O sistema conecta ao servidor MikroTik via SSH
2. **Listagem**: Executa o comando `/ppp profile print detail` para obter todos os perfis
3. **Parsing**: Converte os dados do MikroTik para o formato do banco de dados
4. **Verificação**: Verifica se cada perfil já existe no banco (por nome + servidor)
5. **Importação**: Perfis novos são criados; perfis existentes são ignorados

## Dados Sincronizados

### Do MikroTik para o Banco de Dados

| Campo MikroTik | Campo BD | Conversão |
|----------------|----------|-----------|
| name | name | Direto |
| comment | description | Direto (ou "Sincronizado do Mikrotik") |
| rate-limit | maxBitrateUl / maxBitrateDl | Parseado (ver abaixo) |
| session-timeout | sessionTimeout | Parseado para segundos |
| disabled | active | Invertido (!disabled) |

### Conversão de Rate Limit

O MikroTik usa formato `upload/download` (ex: "10M/20M"):

- **10M** = 10.000.000 bps
- **1G** = 1.000.000.000 bps
- **512K** = 512.000 bps

Exemplo: `"10M/20M"` → Upload: 10000000 bps, Download: 20000000 bps

### Conversão de Session Timeout

O MikroTik pode usar diversos formatos:

- **"2h"** = 7200 segundos
- **"30m"** = 1800 segundos
- **"1d 2h 30m"** = 95400 segundos
- **"3600"** = 3600 segundos

## Exemplo de Requisição

```http
POST http://localhost:8080/api/profiles/sync/server/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

## Exemplo de Resposta

```json
{
  "totalMikrotikUsers": 5,
  "syncedUsers": 3,
  "skippedUsers": 2,
  "failedUsers": 0,
  "syncedUsernames": [
    "Plano-10Mb",
    "Plano-50Mb",
    "Plano-100Mb"
  ],
  "skippedUsernames": [
    "default",
    "default-encryption"
  ],
  "errorMessages": []
}
```

### Campos da Resposta

- **totalMikrotikUsers**: Total de perfis encontrados no MikroTik
- **syncedUsers**: Quantidade de perfis importados com sucesso
- **skippedUsers**: Quantidade de perfis que já existiam no banco
- **failedUsers**: Quantidade de perfis que falharam na importação
- **syncedUsernames**: Lista de nomes dos perfis sincronizados
- **skippedUsernames**: Lista de nomes dos perfis ignorados
- **errorMessages**: Lista de mensagens de erro (se houver)

## Perfis Padrão do MikroTik

O MikroTik vem com alguns perfis padrão que podem aparecer na sincronização:

- **default**: Perfil padrão sem limitações
- **default-encryption**: Perfil padrão com criptografia

Estes perfis serão sincronizados se não existirem no banco. Você pode deletá-los depois se não forem necessários.

## Comportamento

### Perfis Duplicados

Se um perfil com o mesmo nome já existe no banco de dados para aquele servidor:
- O perfil será **pulado** (não atualizado)
- Será contabilizado em `skippedUsers`
- Seu nome aparecerá em `skippedUsernames`

### Perfis sem Rate Limit

Se um perfil no MikroTik não tiver `rate-limit` configurado:
- `maxBitrateUl` e `maxBitrateDl` serão definidos como **0**
- O perfil será criado normalmente

### Perfis sem Session Timeout

Se um perfil não tiver `session-timeout`:
- `sessionTimeout` será definido como **0** (sem limite)

## Workflow Recomendado

### 1. Novo Servidor com Perfis Existentes

```bash
# 1. Cadastrar o servidor MikroTik
POST /api/mikrotik-servers
{
  "name": "Servidor RJ",
  "ipAddress": "192.168.1.1",
  "port": 22,
  "username": "admin",
  "password": "senha123"
}
# Retorna: { "id": 1, ... }

# 2. Sincronizar perfis
POST /api/profiles/sync/server/1

# 3. Sincronizar usuários (usando um perfil padrão)
POST /api/users/sync/server/1/profile/{profileId}
```

### 2. Verificar Perfis Antes da Sincronização

```bash
# 1. Listar perfis do servidor no MikroTik
# (Via SSH ou interface web do MikroTik)

# 2. Listar perfis já cadastrados no banco
GET /api/profiles/server/1

# 3. Sincronizar apenas se necessário
POST /api/profiles/sync/server/1
```

## Casos de Uso

### Caso 1: Servidor Novo com 5 Perfis

**Situação**: Servidor possui 5 perfis configurados, banco de dados vazio.

**Resultado**:
```json
{
  "totalMikrotikUsers": 5,
  "syncedUsers": 5,
  "skippedUsers": 0,
  "failedUsers": 0
}
```

### Caso 2: Re-sincronização

**Situação**: Executar sincronização novamente no mesmo servidor.

**Resultado**:
```json
{
  "totalMikrotikUsers": 5,
  "syncedUsers": 0,
  "skippedUsers": 5,
  "failedUsers": 0
}
```

### Caso 3: Novos Perfis Adicionados

**Situação**: 5 perfis já sincronizados, 3 novos perfis adicionados no MikroTik.

**Resultado**:
```json
{
  "totalMikrotikUsers": 8,
  "syncedUsers": 3,
  "skippedUsers": 5,
  "failedUsers": 0
}
```

## Limitações

1. **Não atualiza perfis existentes**: Perfis que já estão no banco não serão atualizados
2. **Apenas importação**: A sincronização é unidirecional (MikroTik → Banco de Dados)
3. **Requer acesso SSH**: O servidor MikroTik deve estar acessível via SSH
4. **Nome é a chave**: Perfis são identificados pela combinação nome + servidor

## Erros Comuns

### Servidor não encontrado

```json
{
  "timestamp": "2026-01-21T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Servidor Mikrotik não encontrado: 1"
}
```

**Solução**: Verificar se o ID do servidor está correto.

### Falha de conexão SSH

```json
{
  "totalMikrotikUsers": 0,
  "syncedUsers": 0,
  "skippedUsers": 0,
  "failedUsers": 0,
  "errorMessages": [
    "Erro ao buscar profiles do Mikrotik: Falha ao conectar com servidor Mikrotik"
  ]
}
```

**Solução**: Verificar:
- IP e porta do servidor
- Credenciais SSH
- Firewall/rede

### Erro de parsing

```json
{
  "totalMikrotikUsers": 5,
  "syncedUsers": 3,
  "skippedUsers": 1,
  "failedUsers": 1,
  "errorMessages": [
    "Erro ao sincronizar profile Plano-Custom: Invalid rate limit format"
  ]
}
```

**Solução**: Verificar formato dos dados no MikroTik. O profile com erro não será importado, mas os outros sim.

## Segurança

- Requer autenticação com token JWT
- Apenas usuários com perfil **ADMIN** ou **OPERATOR** podem executar
- Credenciais SSH são armazenadas criptografadas
- Logs detalhados de todas as operações

## Performance

- **Tempo estimado**: ~1-2 segundos para 10 perfis
- **Não bloqueia**: Operação síncrona, mas rápida
- **Conexão única**: Usa uma única conexão SSH para toda a operação

## Monitoramento

Logs gerados durante a sincronização:

```
INFO  - Iniciando sincronização de profiles do servidor Servidor RJ (ID: 1)
INFO  - Total de 5 profiles PPPoE encontrados no Mikrotik
DEBUG - Profile default já existe no banco, pulando
INFO  - Profile Plano-10Mb sincronizado com sucesso
INFO  - Profile Plano-50Mb sincronizado com sucesso
INFO  - Sincronização de profiles concluída. Total: 5, Sincronizados: 3, Pulados: 2, Falhas: 0
```

## Integração com Sincronização de Usuários

Após sincronizar os perfis, você pode sincronizar os usuários:

1. **Sincronizar perfis primeiro** para ter todos os perfis disponíveis
2. **Sincronizar usuários** - o sistema tentará associar cada usuário ao perfil correspondente
3. Se um usuário usa um perfil que não existe no banco, o perfil padrão será usado

```bash
# 1. Perfis
POST /api/profiles/sync/server/1

# 2. Usuários (escolher um perfil padrão, ex: ID 1)
POST /api/users/sync/server/1/profile/1
```
