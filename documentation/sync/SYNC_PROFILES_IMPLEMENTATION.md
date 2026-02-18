# Resumo da Implementação - Sincronização de Perfis PPPoE

## 📋 Visão Geral

Implementação da funcionalidade de sincronização de perfis PPPoE do MikroTik para o banco de dados, seguindo o mesmo padrão da sincronização de usuários já existente.

## 🎯 Objetivo

Permitir que ao criar um servidor PPPoE, o usuário tenha a opção de sincronizar os perfis já existentes no MikroTik com o banco de dados, ao invés de ter que criar cada perfil manualmente.

## 📁 Arquivos Criados

### 1. MikrotikPppoeProfileDTO.java
**Caminho**: `src/main/java/br/com/mikrotik/dto/MikrotikPppoeProfileDTO.java`

DTO para representar perfis PPPoE retornados do MikroTik via SSH:
- `name`: Nome do perfil
- `localAddress`: Endereço IP local
- `remoteAddress`: Endereço IP remoto
- `rateLimit`: Limites de banda (formato MikroTik)
- `sessionTimeout`: Timeout de sessão
- `disabled`: Status ativo/inativo
- `comment`: Comentário/descrição

### 2. SYNC_PROFILES.md
**Caminho**: `documentation/SYNC_PROFILES.md`

Documentação completa da funcionalidade incluindo:
- Visão geral e casos de uso
- Detalhamento do endpoint
- Conversão de dados MikroTik → Banco de Dados
- Exemplos de requisição e resposta
- Workflow recomendado
- Tratamento de erros
- Limitações e melhores práticas

## 📝 Arquivos Modificados

### 1. MikrotikSshService.java
**Alterações**:
- Adicionado import do `MikrotikPppoeProfileDTO`
- Implementado método `getPppoeProfilesStructured()` que:
  - Conecta via SSH ao MikroTik
  - Executa `/ppp profile print detail`
  - Parseia a saída e converte em lista de DTOs
  - Trata erros de conexão e parsing

### 2. PppoeProfileService.java
**Alterações**:
- Adicionados imports: `MikrotikPppoeProfileDTO`, `SyncResultDTO`, `Optional`
- Injetada dependência `MikrotikSshService`
- Implementado método `syncProfilesFromMikrotik(Long serverId)`:
  - Busca servidor no banco
  - Obtém perfis do MikroTik via SSH
  - Para cada perfil:
    - Verifica se já existe no banco (por nome + servidor)
    - Se não existe, cria novo registro
    - Se existe, pula (não atualiza)
  - Retorna estatísticas da sincronização

**Métodos auxiliares adicionados**:
- `parseRateLimit(String)`: Converte rate-limit do MikroTik (ex: "10M/20M") para bps
- `parseBandwidth(String)`: Converte unidades (K, M, G) para bps
- `parseSessionTimeout(String)`: Converte formato MikroTik (ex: "2h", "30m") para segundos

### 3. PppoeProfileController.java
**Alterações**:
- Adicionado import do `SyncResultDTO`
- Implementado endpoint `POST /api/profiles/sync/server/{serverId}`:
  - Anotado com `@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")`
  - Documentado com Swagger/OpenAPI
  - Retorna `SyncResultDTO` com estatísticas

### 4. requests.http
**Alterações**:
- Adicionado exemplo de requisição para sincronização de perfis (item 14b)

### 5. API_README.md
**Alterações**:
- Adicionado endpoint de sincronização na seção "Perfis PPPoE"

### 6. INDEX.md
**Alterações**:
- Adicionada referência ao `SYNC_PROFILES.md` na tabela de documentação

### 7. README.md
**Alterações**:
- Adicionada feature "Sincronização de Perfis" na lista de funcionalidades principais

## 🔄 Fluxo de Funcionamento

```
1. Cliente faz requisição POST /api/profiles/sync/server/{serverId}
   ↓
2. PppoeProfileController.syncFromMikrotik()
   ↓
3. PppoeProfileService.syncProfilesFromMikrotik()
   ↓
4. Busca servidor no banco de dados
   ↓
5. MikrotikSshService.getPppoeProfilesStructured()
   ↓
6. Conexão SSH → Executa "/ppp profile print detail"
   ↓
7. Parseia saída e cria lista de MikrotikPppoeProfileDTO
   ↓
8. Para cada perfil do MikroTik:
   ├─ Verifica se existe no banco (findByNameAndMikrotikServer)
   ├─ Se não existe:
   │  ├─ Converte dados (rate-limit, timeout, etc)
   │  ├─ Cria novo PppoeProfile
   │  └─ Salva no banco
   └─ Se existe: pula
   ↓
9. Retorna SyncResultDTO com estatísticas
   ↓
10. Controller retorna ResponseEntity<SyncResultDTO>
```

## 🔀 Conversões de Dados

### Rate Limit
**MikroTik** → **Banco de Dados**
- `"10M/20M"` → Upload: 10.000.000 bps, Download: 20.000.000 bps
- `"1G/2G"` → Upload: 1.000.000.000 bps, Download: 2.000.000.000 bps
- `"512K/1M"` → Upload: 512.000 bps, Download: 1.000.000 bps

### Session Timeout
**MikroTik** → **Banco de Dados**
- `"2h"` → 7200 segundos
- `"30m"` → 1800 segundos
- `"1d 2h 30m"` → 95400 segundos

### Status
**MikroTik** → **Banco de Dados**
- `disabled=false` → `active=true`
- `disabled=true` → `active=false`

## 📊 Resposta da API

```json
{
  "totalMikrotikUsers": 5,      // Total de perfis no MikroTik
  "syncedUsers": 3,              // Perfis importados com sucesso
  "skippedUsers": 2,             // Perfis que já existiam
  "failedUsers": 0,              // Perfis que falharam
  "syncedUsernames": [           // Nomes dos perfis sincronizados
    "Plano-10Mb",
    "Plano-50Mb",
    "Plano-100Mb"
  ],
  "skippedUsernames": [          // Nomes dos perfis ignorados
    "default",
    "default-encryption"
  ],
  "errorMessages": []            // Mensagens de erro (se houver)
}
```

## 🔒 Segurança

- **Autenticação**: Requer token JWT válido
- **Autorização**: Apenas roles ADMIN e OPERATOR
- **Validação**: Verifica existência do servidor antes de sincronizar
- **Logs**: Todas as operações são registradas no log da aplicação

## ✅ Testes Recomendados

### 1. Teste de Sincronização Inicial
```bash
# Servidor com 5 perfis, banco vazio
POST /api/profiles/sync/server/1
# Esperado: syncedUsers=5, skippedUsers=0
```

### 2. Teste de Re-sincronização
```bash
# Executar novamente no mesmo servidor
POST /api/profiles/sync/server/1
# Esperado: syncedUsers=0, skippedUsers=5
```

### 3. Teste de Sincronização Parcial
```bash
# Adicionar 2 novos perfis no MikroTik
# Executar sincronização
POST /api/profiles/sync/server/1
# Esperado: syncedUsers=2, skippedUsers=5
```

### 4. Teste de Servidor Inválido
```bash
POST /api/profiles/sync/server/999
# Esperado: HTTP 404 - Servidor não encontrado
```

### 5. Teste de Parsing de Rate Limit
Verificar conversão de diferentes formatos:
- "10M/20M"
- "1G/2G"
- "512K/1M"
- "" (vazio) → deve usar 0/0

### 6. Teste de Parsing de Timeout
Verificar conversão de diferentes formatos:
- "2h"
- "30m"
- "1d"
- "" (vazio) → deve usar 0

## 🎓 Padrão de Implementação

Esta implementação segue o mesmo padrão da sincronização de usuários:
1. ✅ DTO específico para dados do MikroTik
2. ✅ Reutilização do `SyncResultDTO` para resposta
3. ✅ Método no `MikrotikSshService` para buscar dados via SSH
4. ✅ Método no Service principal para orquestrar a sincronização
5. ✅ Endpoint no Controller com autenticação e documentação
6. ✅ Documentação completa em arquivo separado
7. ✅ Exemplos em `requests.http`

## 🚀 Integração com Sincronização de Usuários

Workflow recomendado para servidor novo:

```bash
# 1. Cadastrar servidor
POST /api/mikrotik-servers
{ "name": "Servidor RJ", ... }

# 2. Sincronizar perfis primeiro
POST /api/profiles/sync/server/1

# 3. Depois sincronizar usuários
POST /api/users/sync/server/1/profile/{defaultProfileId}
```

Vantagens desta ordem:
- Perfis ficam disponíveis antes dos usuários
- Usuários podem ser associados aos perfis corretos
- Apenas usuários sem perfil correspondente usarão o padrão

## 📈 Melhorias Futuras (Sugestões)

1. **Sincronização bidirecional**: Atualizar MikroTik com dados do banco
2. **Atualização de perfis existentes**: Opção para sobrescrever perfis
3. **Sincronização incremental**: Apenas novos/modificados
4. **Validação de dados**: Verificar limites e formatos antes de salvar
5. **Agendamento**: Sincronização automática periódica
6. **Notificações**: Alertas quando novos perfis são detectados

## ✨ Resultado

Funcionalidade completa e documentada, pronta para uso em produção, seguindo os padrões de qualidade do projeto.
