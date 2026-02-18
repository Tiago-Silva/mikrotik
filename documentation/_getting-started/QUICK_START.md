# Guia de Quick Start - Mikrotik PPPoE Management API

## 🚀 Início Rápido (5 minutos)

### 1. Iniciar MySQL com Docker
```bash
# Na raiz do projeto
docker-compose up -d

# Verificar se está rodando
docker-compose ps
```

MySQL estará em `localhost:3306` com:
- User: `root` / Senha: `root`
- Database: `mikrotik_db`
- PhpMyAdmin: `http://localhost:8081`

### 2. Compilar e Executar a API
```bash
mvn clean install
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

### 3. Acessar Swagger UI
Abra no navegador: `http://localhost:8080/swagger-ui.html`

### 4. Fazer Login
**Usuários padrão criados automaticamente:**
- Admin: `admin` / `admin123`
- Operator: `operator` / `operator123`
- Viewer: `viewer` / `viewer123`

Endpoint: `POST /api/auth/login`
```json
{
  "username": "admin",
  "password": "admin123"
}
```

Você receberá um token JWT que deve ser usado em todas as outras requisições no header:
```
Authorization: Bearer <seu-token>
```

### 5. Testar a API

#### Criar Servidor Mikrotik
```bash
curl -X POST http://localhost:8080/api/mikrotik-servers \
  -H "Authorization: Bearer <seu-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Servidor Principal",
    "ipAddress": "192.168.1.1",
    "port": 22,
    "username": "admin",
    "password": "password123",
    "description": "Servidor Mikrotik principal"
  }'
```

#### Criar Perfil PPPoE
```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Authorization: Bearer <seu-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Plano 10Mb",
    "description": "10 Mbps download / 5 Mbps upload",
    "maxBitrateDl": 10000000,
    "maxBitrateUl": 5000000,
    "sessionTimeout": 3600,
    "mikrotikServerId": 1
  }'
```

#### Criar Usuário PPPoE
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <seu-token>" \
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

## 📝 Estrutura do Projeto

```
mikrotik/
├── src/
│   ├── main/
│   │   ├── java/br/com/mikrotik/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/             # Business Logic
│   │   │   ├── repository/          # Data Access
│   │   │   ├── model/               # JPA Entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── exception/           # Custom Exceptions
│   │   │   ├── security/            # JWT & Security
│   │   │   ├── config/              # Spring Configuration
│   │   │   └── MikrotikApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── docker-compose.yml
├── schema.sql
├── API_README.md
└── QUICK_START.md (este arquivo)
```

## 🔐 Roles e Permissões

| Endpoint | ADMIN | OPERATOR | VIEWER |
|----------|-------|----------|--------|
| `/api/auth/**` | ✓ | ✓ | ✓ |
| `/api/mikrotik-servers/**` | ✓ | ✗ | ✗ |
| `/api/profiles/**` | ✓ | ✓ | Read |
| `/api/users/**` | ✓ | ✓ | Read |
| `/api/connections/**` | ✓ | ✓ | ✓ |

## 🆘 Troubleshooting

### Erro: "Connection refused"
MySQL não está rodando. Execute:
```bash
docker-compose up -d
docker-compose logs mysql
```

### Erro: "Access denied for user 'root'"
Verificar credentials no `application.properties`

### Erro: "JWT token invalid"
Token expirou. Faça novo login.

### Erro ao conectar com Mikrotik
Verificar:
1. IP e porta corretos
2. SSH habilitado em Mikrotik
3. Credenciais corretas

## 📚 Recursos Adicionais

- [Documentação API Completa](API_README.md)
- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security Docs](https://spring.io/projects/spring-security)

## 💡 Próximos Passos

1. Integrar com seu Mikrotik real
2. Customizar perfis conforme necessário
3. Implementar webhooks para eventos
4. Adicionar relatórios e dashboards
5. Implementar cache com Redis
6. Adicionar testes unitários e integração

## 📞 Suporte

Para dúvidas ou problemas, verifique os logs:
```bash
# Logs em tempo real
tail -f /var/log/mikrotik-api.log

# Via Docker
docker-compose logs -f app
```

---
**Última atualização:** 2026-01-20
