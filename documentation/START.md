# 🎯 QUICK START - MIKROTIK PPPOE MANAGEMENT API

## ⚡ Em 3 Passos Simples

### 1️⃣ Iniciar Infraestrutura
```bash
cd /home/tiago/workspace-intelij-idea/youtube/mikrotik
docker-compose up -d
```
✅ MySQL rodando em localhost:3306  
✅ PHPMyAdmin em http://localhost:8081  

### 2️⃣ Compilar e Executar
```bash
mvn clean install
mvn spring-boot:run
```
✅ App rodando em http://localhost:8080  
✅ Swagger em http://localhost:8080/swagger-ui.html  

### 3️⃣ Testar Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
✅ Usuário padrão já criado  
✅ Receba JWT token  

---

## 👥 Usuários Padrão

| Usuário | Senha | Role |
|---------|-------|------|
| admin | admin123 | ADMIN |
| operator | operator123 | OPERATOR |
| viewer | viewer123 | VIEWER |

---

## 🔗 Links Rápidos

| Link | Descrição |
|------|-----------|
| http://localhost:8080/swagger-ui.html | 📖 Documentação Interativa |
| http://localhost:8081 | 🗄️ PHPMyAdmin |
| http://localhost:8080/v3/api-docs | 📋 OpenAPI JSON |

---

## 📚 Documentação

1. **QUICK_START.md** - Este guia
2. **API_README.md** - Documentação completa
3. **DEVELOPMENT.md** - Guia de desenvolvimento
4. **ARCHITECTURE.md** - Diagramas e fluxos
5. **README_PTBR.md** - Sumário em português

---

## 🆘 Problemas Comuns

### "Conexão recusada em 3306"
```bash
# Verificar se Docker está rodando
docker-compose ps
# Reiniciar se necessário
docker-compose down
docker-compose up -d
```

### "Port 8080 já em uso"
Editar em `application.properties`:
```properties
server.port=8081
```

### "Token JWT inválido"
Fazer novo login para obter novo token

---

## 📊 Estrutura do Projeto

```
mikrotik/
├── src/main/java/br/com/mikrotik/
│   ├── controller/ (5 classes REST)
│   ├── service/ (7 classes de lógica)
│   ├── repository/ (6 classes de dados)
│   ├── model/ (6 entidades)
│   ├── dto/ (7 classes de transferência)
│   ├── exception/ (tratamento de erros)
│   ├── security/ (JWT + autenticação)
│   └── config/ (configurações)
├── docker-compose.yml (MySQL + PHPMyAdmin)
├── schema.sql (banco de dados)
└── documentação/ (9 arquivos .md)
```

---

## ✨ Funcionalidades

✅ **Autenticação JWT**  
✅ **CRUD Completo:**
   - Servidores Mikrotik (6 endpoints)
   - Perfis PPPoE (6 endpoints)
   - Usuários PPPoE (8 endpoints)
   - Conexões (6 endpoints)
✅ **Integração SSH com Mikrotik**  
✅ **Auditoria e Logging**  
✅ **Swagger UI**  
✅ **Roles: ADMIN, OPERATOR, VIEWER**  

---

## 🔒 Segurança

✅ JWT com expiração de 24h  
✅ Criptografia BCrypt  
✅ Validação em DTOs  
✅ Autorização por role  
✅ Erro handling centralizado  

---

## 📝 Exemplos Rápidos

### Criar Servidor
```bash
curl -X POST http://localhost:8080/api/mikrotik-servers \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Servidor1",
    "ipAddress": "192.168.1.1",
    "port": 22,
    "username": "admin",
    "password": "password"
  }'
```

### Listar Usuários
```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer TOKEN"
```

### Criar Usuário PPPoE
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "cliente1",
    "password": "senha123",
    "email": "cliente@example.com",
    "profileId": 1,
    "mikrotikServerId": 1
  }'
```

---

## 🧪 Testar Tudo Automaticamente

```bash
# Script bash com exemplos
chmod +x test-api.sh
./test-api.sh

# Ou usar REST Client do VS Code com:
requests.http
```

---

## 📊 Banco de Dados

**Tabelas:**
- api_users
- mikrotik_servers
- pppoe_profiles
- pppoe_users
- pppoe_connections
- audit_logs

**Acesso PHPMyAdmin:**
- http://localhost:8081
- Usuário: root
- Senha: root

---

## 🚀 Verificar Implementação

```bash
chmod +x verify-implementation.sh
./verify-implementation.sh
```

Valida se todos os 50+ arquivos foram criados corretamente.

---

## 📞 Recursos

| Necessidade | Arquivo |
|-----------|---------|
| Começar já | QUICK_START.md |
| API completa | API_README.md |
| Desenvolvimento | DEVELOPMENT.md |
| Arquitetura | ARCHITECTURE.md |
| Sumário | README_PTBR.md |

---

## ✅ Checklist de Verificação

```
□ Docker iniciado (docker-compose up -d)
□ Maven instalado (mvn --version)
□ Java 21 instalado (java --version)
□ Banco de dados criado
□ App rodando (localhost:8080)
□ Swagger acessível (swagger-ui.html)
□ Conseguiu fazer login
```

---

## 🎊 Pronto!

Você agora tem uma **API REST profissional** para gerenciar Mikrotik com PPPoE.

**Próximo passo?** Abra http://localhost:8080/swagger-ui.html e explore! 🚀

---

**Versão:** 1.0.0  
**Status:** ✅ Pronto para Usar  
**Data:** Janeiro 2026  

