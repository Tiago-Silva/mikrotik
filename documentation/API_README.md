# Mikrotik PPPoE Management API

API completa para gerenciar servidores Mikrotik com protocolo PPPoE, desenvolvida em Spring Boot.

## 📋 Funcionalidades

### ✅ Autenticação e Autorização
- Login com JWT Token
- Controle de acesso por roles (ADMIN, OPERATOR, VIEWER)
- Integração com Spring Security

### ✅ Gerencimento de Servidores Mikrotik
- Adicionar/editar/remover servidores Mikrotik
- Testar conectividade com servidor
- Suporte a múltiplos servidores

### ✅ Gerenciamento de Perfis PPPoE
- Criar perfis com limites de banda (download/upload)
- Configurar timeout de sessão
- Ativar/desativar perfis

### ✅ Gerenciamento de Usuários PPPoE
- Criar usuários PPPoE no servidor Mikrotik
- Gerenciar email e comentários
- Ativar/desativar usuários sem deletar
- Listar usuários com paginação

### ✅ Monitoramento de Conexões
- Acompanhar conexões ativas
- Ver estatísticas de uso (bytes up/down)
- Histórico de conexões

### ✅ Auditoria
- Log de todas as operações realizadas
- Rastreabilidade de mudanças

### ✅ Documentação Interativa
- Swagger UI integrado
- OpenAPI 3.0

## 🛠️ Tecnologias

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA**
- **Spring Security + JWT**
- **MySQL 8.0**
- **Lombok**
- **JSch (SSH)**
- **SpringDoc OpenAPI (Swagger)**
- **Maven**

## 📦 Pré-requisitos

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Servidor Mikrotik com SSH habilitado

## 🚀 Instalação e Configuração

### 1. Clonar o repositório
```bash
git clone <repository-url>
cd mikrotik
```

### 2. Configurar banco de dados MySQL
```sql
CREATE DATABASE mikrotik_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Atualizar arquivo de configuração
Editar `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mikrotik_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# JWT
jwt.secret=sua-chave-secreta-segura
jwt.expiration=86400000
```

### 4. Compilar e executar
```bash
mvn clean install
mvn spring-boot:run
```

## 📚 API Endpoints

### Autenticação
```
POST   /api/auth/login                 - Login de usuário
GET    /api/auth/validate?token=...   - Validar token JWT
```

### Servidores Mikrotik
```
POST   /api/mikrotik-servers           - Criar novo servidor
GET    /api/mikrotik-servers           - Listar todos os servidores
GET    /api/mikrotik-servers/{id}      - Obter servidor específico
PUT    /api/mikrotik-servers/{id}      - Atualizar servidor
DELETE /api/mikrotik-servers/{id}      - Deletar servidor
POST   /api/mikrotik-servers/{id}/test-connection - Testar conexão
```

### Perfis PPPoE
```
POST   /api/profiles                   - Criar novo perfil
GET    /api/profiles                   - Listar todos os perfis
GET    /api/profiles/{id}              - Obter perfil específico
GET    /api/profiles/server/{serverId} - Listar perfis de servidor
PUT    /api/profiles/{id}              - Atualizar perfil
DELETE /api/profiles/{id}              - Deletar perfil
```

### Usuários PPPoE
```
POST   /api/users                      - Criar novo usuário
GET    /api/users                      - Listar todos os usuários
GET    /api/users/{id}                 - Obter usuário específico
GET    /api/users/server/{serverId}    - Listar usuários de servidor (com paginação)
PUT    /api/users/{id}                 - Atualizar usuário
DELETE /api/users/{id}                 - Deletar usuário
POST   /api/users/{id}/disable         - Desativar usuário
POST   /api/users/{id}/enable          - Ativar usuário
POST   /api/users/sync/server/{serverId}/profile/{profileId} - Sincronizar usuários do Mikrotik
```

### Conexões PPPoE
```
GET    /api/connections                - Listar todas as conexões
GET    /api/connections/{id}           - Obter conexão específica
GET    /api/connections/user/{userId}  - Listar conexões de usuário
GET    /api/connections/server/{serverId} - Listar conexões de servidor
GET    /api/connections/active/count   - Contar conexões ativas
GET    /api/connections/server/{serverId}/active - Listar conexões ativas
```

## 🔐 Roles e Permissões

| Role     | Servidores | Perfis | Usuários | Conexões |
|----------|-----------|--------|----------|----------|
| ADMIN    | CRUD      | CRUD   | CRUD     | Read     |
| OPERATOR | -         | CRUD   | CRUD     | Read     |
| VIEWER   | -         | Read   | Read     | Read     |

## 📝 Exemplos de Uso

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

### 2. Criar Servidor Mikrotik
```bash
curl -X POST http://localhost:8080/api/mikrotik-servers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Servidor 1",
    "ipAddress": "192.168.1.1",
    "port": 22,
    "username": "admin",
    "password": "password",
    "description": "Servidor principal"
  }'
```

### 3. Criar Perfil PPPoE
```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Plano 10Mb",
    "description": "Plano 10 Mbps",
    "maxBitrateDl": 10000000,
    "maxBitrateUl": 5000000,
    "sessionTimeout": 3600,
    "mikrotikServerId": 1
  }'
```

### 4. Criar Usuário PPPoE
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente1",
    "password": "senha123",
    "email": "cliente1@example.com",
    "comment": "Cliente de teste",
    "profileId": 1,
    "mikrotikServerId": 1
  }'
```

### 5. Sincronizar Usuários Existentes do Mikrotik
```bash
# Sincronizar todos os usuários PPPoE já existentes no Mikrotik com o banco de dados
# Ideal quando você já tem usuários criados no Mikrotik e quer importá-los
curl -X POST http://localhost:8080/api/users/sync/server/1/profile/1 \
  -H "Authorization: Bearer <token>"

# Resposta:
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

> **Nota**: A sincronização é útil quando você já possui usuários PPPoE criados diretamente no Mikrotik e deseja 
> importá-los para o banco de dados sem precisar criar um por um. Usuários que já existem no banco serão ignorados 
> automaticamente. O `profileId` é usado como perfil padrão para usuários que não têm um perfil correspondente no banco.

## 📊 Estrutura do Banco de Dados

### Tabelas

- **mikrotik_servers**: Servidores Mikrotik
- **pppoe_profiles**: Perfis de PPPoE
- **pppoe_users**: Usuários PPPoE
- **pppoe_connections**: Registro de conexões
- **api_users**: Usuários da API
- **audit_logs**: Log de auditoria

## 🔌 Integração com Mikrotik

A API se comunica com Mikrotik via SSH utilizando a biblioteca JSch:

```java
// Exemplo de criação de usuário no Mikrotik
sshService.createPppoeUser(
    "192.168.1.1",     // IP
    22,                // Port
    "admin",           // Username
    "password",        // Password
    "user1",           // PPPoE Username
    "pass123",         // PPPoE Password
    "Plano 10Mb"       // Profile name
);
```

## 🧪 Testes

```bash
# Executar testes
mvn test

# Com cobertura
mvn clean test jacoco:report
```

## 📖 Documentação Interativa

Acesse a documentação Swagger em:
```
http://localhost:8080/swagger-ui.html
```

## 🐛 Troubleshooting

### Erro: "Falha ao conectar com servidor Mikrotik"
- Verificar IP e porta do servidor
- Confirmar que SSH está habilitado em Mikrotik
- Validar credenciais de acesso

### Erro: "Token JWT inválido"
- Verificar se token ainda é válido
- Fazer novo login

### Erro: "Username já existe neste servidor"
- Usuário PPPoE já foi criado
- Usar outro username

## 📄 Licença

Apache License 2.0

## 👤 Autor

Desenvolvido por Tiago

## 📞 Suporte

Para dúvidas ou problemas, abra uma issue no repositório.
