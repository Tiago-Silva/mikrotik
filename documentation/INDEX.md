# 📚 Índice de Recursos - Mikrotik PPPoE Management API

> **💡 NOVO:** Para uma navegação completa e organizada por perfil, veja **[DOCS_INDEX.md](DOCS_INDEX.md)**

## 📖 Documentação

| Arquivo | Descrição |
|---------|-----------|
| [DOCS_INDEX.md](DOCS_INDEX.md) | 🆕 **Índice master com trilhas por perfil** |
| [README.md](README.md) | 📚 **Porta de entrada da documentação** |
| [API_README.md](API_README.md) | 📘 Documentação completa da API com exemplos de uso |
| [QUICK_START.md](QUICK_START.md) | ⚡ Guia de início rápido (5 minutos) |
| [DEVELOPMENT.md](DEVELOPMENT.md) | 🛠️ Guia de desenvolvimento e padrões |
| [SYNC_USERS.md](SYNC_USERS.md) | 🔄 Guia de sincronização de usuários PPPoE do Mikrotik |
| [SYNC_PROFILES.md](SYNC_PROFILES.md) | 🔄 Guia de sincronização de perfis PPPoE do Mikrotik |
| [ROADMAP.md](ROADMAP.md) | 🗺️ Roadmap de desenvolvimento completo |
| [SUMMARY.md](SUMMARY.md) | 📊 Sumário executivo do projeto |
| [LINKEDIN_POST.md](LINKEDIN_POST.md) | 💼 Posts para LinkedIn (4 versões) |
| [INDEX.md](INDEX.md) | 📚 Este arquivo |

## 🔧 Configuração e Instalação

| Arquivo | Descrição |
|---------|-----------|
| [pom.xml](../pom.xml) | Maven - Dependências do projeto |
| [application.properties](../src/main/resources/application.properties.txt) | Configurações da aplicação |
| [.env.example](../.env.example) | Variáveis de ambiente (template) |
| [docker-compose.yml](../docker-compose.yml) | Docker - Banco de dados e PHPMyAdmin |
| [schema.sql](schema.sql) | SQL - Estrutura do banco de dados |

## 🧪 Testes e Exemplos

| Arquivo | Descrição |
|---------|-----------|
| [test-api.sh](../test-api.sh) | 🐚 Script bash com exemplos de requisições |
| [requests.http](requests.http) | 📝 Exemplos HTTP para REST Client |

## 💻 Código-Fonte

### Controllers (camada web)
```
src/main/java/br/com/mikrotik/controller/
├── AuthController.java                 # Login e autenticação
├── CompanyController.java              # CRUD de empresas (multi-tenant)
├── CustomerController.java             # CRUD de clientes (CRM)
├── MikrotikServerController.java       # CRUD de servidores
├── PppoeProfileController.java         # CRUD de perfis
├── PppoeUserController.java            # CRUD de usuários
└── PppoeConnectionController.java      # Monitoramento de conexões
```

### Services (lógica de negócio)
```
src/main/java/br/com/mikrotik/service/
├── CompanyService.java                 # Lógica de empresas
├── CustomerService.java                # Lógica de clientes (CRM)
├── MikrotikServerService.java          # Lógica de servidores
├── MikrotikSshService.java             # Comunicação SSH com Mikrotik
├── PppoeProfileService.java            # Lógica de perfis
├── PppoeUserService.java               # Lógica de usuários
└── PppoeConnectionService.java         # Lógica de conexões
├── MikrotikSshService.java             # Integração SSH com Mikrotik
└── CustomUserDetailsService.java       # Autenticação customizada
```

### Repositories (acesso a dados)
```
src/main/java/br/com/mikrotik/repository/
├── ApiUserRepository.java
├── MikrotikServerRepository.java
├── PppoeProfileRepository.java
├── PppoeUserRepository.java
├── PppoeConnectionRepository.java
└── AuditLogRepository.java
```

### Models (entidades JPA)
```
src/main/java/br/com/mikrotik/model/
├── ApiUser.java                        # Usuários da API
├── MikrotikServer.java                 # Servidores Mikrotik
├── PppoeProfile.java                   # Perfis PPPoE
├── PppoeUser.java                      # Usuários PPPoE
├── PppoeConnection.java                # Conexões ativas
└── AuditLog.java                       # Log de auditoria
```

### DTOs (transferência de dados)
```
src/main/java/br/com/mikrotik/dto/
├── LoginDTO.java
├── LoginResponseDTO.java
├── MikrotikServerDTO.java
├── PppoeProfileDTO.java
├── PppoeUserDTO.java
└── PppoeConnectionDTO.java
```

### Segurança
```
src/main/java/br/com/mikrotik/security/
├── JwtTokenProvider.java               # Geração e validação JWT
└── JwtAuthenticationFilter.java        # Filtro de autenticação
```

### Configuração
```
src/main/java/br/com/mikrotik/config/
├── SecurityConfig.java                 # Segurança Spring
├── OpenApiConfig.java                  # Swagger/OpenAPI
└── DataInitializationConfig.java       # Inicialização de dados
```

### Exceções
```
src/main/java/br/com/mikrotik/exception/
├── ResourceNotFoundException.java      # Recurso não encontrado
├── MikrotikConnectionException.java    # Erro de conexão
├── ApiError.java                       # Modelo de erro
└── GlobalExceptionHandler.java         # Handler centralizado
```

### Constantes
```
src/main/java/br/com/mikrotik/constant/
└── ApiConstants.java                   # Constantes da API
```

## 🚀 Quick Links

| Link | Descrição |
|------|-----------|
| `http://localhost:8080/swagger-ui.html` | 📖 Documentação Swagger interativa |
| `http://localhost:8080/v3/api-docs` | 📋 OpenAPI JSON specs |
| `http://localhost:8081` | 🗄️ PHPMyAdmin (quando usando Docker) |

## 🔑 Funcionalidades Principais

### ✅ Autenticação
- [x] Login com JWT Token
- [x] Validação de token
- [x] Refresh token
- [x] Controle de acesso por role

### ✅ Gerenciamento de Servidores
- [x] Criar servidor Mikrotik
- [x] Listar servidores
- [x] Atualizar servidor
- [x] Deletar servidor
- [x] Testar conectividade SSH

### ✅ Gerenciamento de Perfis
- [x] Criar perfil PPPoE
- [x] Listar perfis
- [x] Atualizar perfil
- [x] Deletar perfil
- [x] Filtrar por servidor

### ✅ Gerenciamento de Usuários
- [x] Criar usuário PPPoE (sincronizar com Mikrotik)
- [x] Listar usuários
- [x] Atualizar usuário
- [x] Deletar usuário (remover do Mikrotik)
- [x] Ativar/Desativar usuário
- [x] Paginação e filtros

### ✅ Monitoramento de Conexões
- [x] Rastrear conexões ativas
- [x] Ver estatísticas de uso
- [x] Histórico de desconexões
- [x] Contar conexões por servidor

### ✅ Auditoria
- [x] Registrar todas as operações
- [x] Rastrear mudanças
- [x] Histórico de acesso

## 📊 Endpoints por Categoria

### Autenticação (2 endpoints)
```
POST   /api/auth/login          - Fazer login
GET    /api/auth/validate       - Validar token
```

### Servidores (6 endpoints)
```
POST   /api/mikrotik-servers
GET    /api/mikrotik-servers
GET    /api/mikrotik-servers/{id}
PUT    /api/mikrotik-servers/{id}
DELETE /api/mikrotik-servers/{id}
POST   /api/mikrotik-servers/{id}/test-connection
```

### Perfis (6 endpoints)
```
POST   /api/profiles
GET    /api/profiles
GET    /api/profiles/{id}
GET    /api/profiles/server/{serverId}
PUT    /api/profiles/{id}
DELETE /api/profiles/{id}
```

### Usuários (8 endpoints)
```
POST   /api/users
GET    /api/users
GET    /api/users/{id}
GET    /api/users/server/{serverId}
PUT    /api/users/{id}
DELETE /api/users/{id}
POST   /api/users/{id}/disable
POST   /api/users/{id}/enable
```

### Conexões (6 endpoints)
```
GET    /api/connections
GET    /api/connections/{id}
GET    /api/connections/user/{userId}
GET    /api/connections/server/{serverId}
GET    /api/connections/active/count
GET    /api/connections/server/{serverId}/active
```

**Total: 28 endpoints REST**

## 🎯 Casos de Uso

### 1. Novo Operador Chegando
1. Ler [QUICK_START.md](QUICK_START.md)
2. Executar `docker-compose up -d`
3. Executar `mvn spring-boot:run`
4. Fazer login em http://localhost:8080/swagger-ui.html
5. Explorar API

### 2. Desenvolvedor Adicionando Feature
1. Ler [DEVELOPMENT.md](DEVELOPMENT.md)
2. Criar branch: `git checkout -b feature/minha-feature`
3. Seguir padrões documentados
4. Testar com [requests.http](requests.http) ou [test-api.sh](../test-api.sh)
5. Criar Pull Request

### 3. DevOps Deployando em Produção
1. Ler [API_README.md](API_README.md) seção "Produção"
2. Configurar `.env` com valores reais
3. Usar Docker: `docker build -t mikrotik:1.0.0 .`
4. Deploy com orquestrador (Kubernetes, Docker Swarm, etc)

### 4. Operador Gerenciando Usuarios
1. Acessar Swagger: http://localhost:8080/swagger-ui.html
2. Fazer login com credenciais (admin/admin123)
3. Usar endpoints `/api/users` para CRUD
4. Usar `/api/connections` para monitorar

## 📋 Checklist de Deployment

- [ ] Banco de dados configurado e testado
- [ ] `.env` criado com valores de produção
- [ ] `jwt.secret` alterado para valor único
- [ ] SSL/HTTPS habilitado
- [ ] Firewall configurado
- [ ] Backups configurados
- [ ] Logs centralizados
- [ ] Monitoring e alertas ativados
- [ ] Testes de carga executados

## 🆘 Troubleshooting Rápido

| Problema | Solução |
|----------|---------|
| Conexão recusada | Verificar se MySQL está rodando |
| Token inválido | Fazer novo login |
| Usuário não encontrado | Verificar ID correto |
| Erro SSH | Verificar IP, porta e credenciais |
| Port 8080 em uso | Alterar em `application.properties` |

## 📞 Suporte

- **Documentação:** Leia [API_README.md](API_README.md)
- **Quick Start:** Leia [QUICK_START.md](QUICK_START.md)
- **Desenvolvimento:** Leia [DEVELOPMENT.md](DEVELOPMENT.md)
- **Issues:** Abra issue no repositório

## 🎓 Aprendizado

```
Iniciante?          → Leia QUICK_START.md
Desenvolvedor?      → Leia DEVELOPMENT.md
Admin/Operador?     → Leia API_README.md
Arquiteto?          → Leia SUMMARY.md
```

## ✨ O que Falta (Sugestões)

- [ ] Testes automatizados completos
- [ ] Cache com Redis
- [ ] Webhook para eventos
- [ ] Dashboard web (Frontend React/Vue)
- [ ] API versioning
- [ ] Rate limiting
- [ ] Multi-language support
- [ ] Backup automático
- [ ] Kubernetes deployment

## 📈 Histórico de Versões

- **v1.0.0** (Janeiro 2026) - Release inicial com funcionalidades completas

---

**Última atualização:** Janeiro 2026
**Mantido por:** Tiago
**Status:** ✅ Pronto para Produção

