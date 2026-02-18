# 📚 API de Gerenciamento de Usuários
## 📋 Visão Geral
Sistema completo de CRUD para gerenciamento de usuários da API com controle hierárquico de permissões, multi-tenant e recursos de segurança avançados.
---
## 🎯 Funcionalidades Implementadas
✅ **CRUD Completo** de usuários
✅ **Hierarquia de Permissões** (ADMIN > OPERATOR > FINANCIAL/TECHNICAL > VIEWER)
✅ **Multi-tenant** (isolamento por empresa)
✅ **Troca de Senha** (usuário próprio)
✅ **Reset de Senha** (apenas admin)
✅ **Ativar/Desativar** usuários
✅ **Soft Delete** (não exclui, apenas desativa)
✅ **Validações de Segurança** (não pode deletar/desativar a si mesmo)
✅ **Criptografia BCrypt** para senhas
✅ **Rastreamento de Login** (last_login)
✅ **Timestamps Automáticos** (created_at, updated_at)
✅ **Documentação Swagger** completa
---
## 🔐 Hierarquia de Permissões (UserRole)
```
ADMIN (100)       → Acesso completo ao sistema
  ↓
OPERATOR (75)     → Operações do dia a dia
  ↓
FINANCIAL (50)    → Módulo financeiro
TECHNICAL (50)    → Módulo técnico/Mikrotik
  ↓
VIEWER (25)       → Apenas visualização
```
### Métodos do Enum UserRole
- `isAdmin()` - Verifica se é administrador
- `canOperate()` - Verifica se pode realizar operações
- `hasFinancialAccess()` - Verifica acesso financeiro
- `hasTechnicalAccess()` - Verifica acesso técnico
- `hasPermissionLevel(UserRole)` - Compara níveis de permissão
---
## 📊 Endpoints Disponíveis
### **1. Criar Usuário**
```http
POST /api/users
Authorization: Bearer {token}
Content-Type: application/json
```
**Permissões:** ADMIN, OPERATOR
**Body:**
```json
{
  "username": "maria.silva",
  "password": "senha123",
  "email": "maria@example.com",
  "role": "OPERATOR",
  "active": true
}
```
**Resposta:** `201 Created`
```json
{
  "id": 5,
  "companyId": 1,
  "username": "maria.silva",
  "email": "maria@example.com",
  "role": "OPERATOR",
  "roleDisplayName": "Operador",
  "roleDescription": "Acesso para operações do dia a dia",
  "active": true,
  "createdAt": "2026-02-11T10:30:00",
  "updatedAt": "2026-02-11T10:30:00",
  "lastLogin": null
}
```
---
### **2. Listar Todos os Usuários (Paginado)**
```http
GET /api/users?page=0&size=20&sort=username,asc
Authorization: Bearer {token}
```
**Permissões:** ADMIN, OPERATOR, VIEWER
**Resposta:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "companyId": 1,
      "username": "admin",
      "email": "admin@example.com",
      "role": "ADMIN",
      "roleDisplayName": "Administrador",
      "active": true,
      "createdAt": "2026-01-01T10:00:00",
      "lastLogin": "2026-02-11T09:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```
---
### **3. Buscar Usuário por ID**
```http
GET /api/users/{id}
Authorization: Bearer {token}
```
**Permissões:** ADMIN, OPERATOR, VIEWER
**Resposta:** `200 OK`
---
### **4. Buscar por Username**
```http
GET /api/users/username/{username}
Authorization: Bearer {token}
```
**Permissões:** ADMIN, OPERATOR
**Resposta:** `200 OK`
---
### **5. Listar Usuários Ativos**
```http
GET /api/users/active?page=0&size=20
Authorization: Bearer {token}
```
**Permissões:** ADMIN, OPERATOR, VIEWER
**Resposta:** `200 OK` (Paginado)
---
### **6. Listar por Role**
```http
GET /api/users/role/{role}
Authorization: Bearer {token}
```
**Permissões:** ADMIN, OPERATOR
**Valores válidos:** ADMIN, OPERATOR, FINANCIAL, TECHNICAL, VIEWER
**Resposta:** `200 OK`
---
### **7. Listar Todas as Roles**
```http
GET /api/users/roles
Authorization: Bearer {token}
```
**Permissões:** TODOS
**Resposta:** `200 OK`
```json
[
  {
    "name": "ADMIN",
    "displayName": "Administrador",
    "description": "Acesso completo ao sistema",
    "level": 100
  },
  {
    "name": "OPERATOR",
    "displayName": "Operador",
    "description": "Acesso para operações do dia a dia",
    "level": 75
  }
]
```
---
### **8. Atualizar Usuário**
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json
```
**Permissões:** ADMIN, OPERATOR
**Body:**
```json
{
  "username": "maria.silva",
  "email": "maria.novo@example.com",
  "role": "FINANCIAL",
  "active": true
}
```
**Resposta:** `200 OK`
---
### **9. Alterar Própria Senha**
```http
PATCH /api/users/{id}/change-password
Authorization: Bearer {token}
Content-Type: application/json
```
**Permissões:** TODOS (apenas própria senha)
**Body:**
```json
{
  "currentPassword": "senha123",
  "newPassword": "novaSenha456",
  "confirmPassword": "novaSenha456"
}
```
**Resposta:** `204 No Content`
---
### **10. Resetar Senha (Admin)**
```http
PATCH /api/users/{id}/reset-password
Authorization: Bearer {token}
Content-Type: application/json
```
**Permissões:** ADMIN apenas
**Body:**
```json
{
  "newPassword": "senhaNova123"
}
```
**Resposta:** `204 No Content`
---
### **11. Ativar/Desativar Usuário**
```http
PATCH /api/users/{id}/toggle-active
Authorization: Bearer {token}
```
**Permissões:** ADMIN apenas
**Resposta:** `200 OK` (retorna usuário atualizado)
---
### **12. Deletar Usuário (Soft Delete)**
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
```
**Permissões:** ADMIN apenas
**Resposta:** `204 No Content`
**Nota:** Não exclui o usuário, apenas desativa (active = false)
---
## 🛡️ Regras de Segurança
### **Validações Automáticas**
1. ✅ **Username único** por empresa
2. ✅ **Email único** globalmente
3. ✅ **Senha mínima** de 6 caracteres
4. ✅ **Senha sempre criptografada** (BCrypt)
5. ✅ **Company ID** obrigatório (multi-tenant)
6. ✅ **Role obrigatória** (default: VIEWER)
### **Proteções de Permissão**
1. ✅ **Não pode criar role superior** à sua própria
2. ✅ **Não pode editar usuário** com permissão maior/igual
3. ✅ **Apenas admin** pode resetar senhas
4. ✅ **Apenas admin** pode ativar/desativar
5. ✅ **Apenas admin** pode deletar
6. ✅ **Não pode deletar/desativar** a si mesmo
### **Hierarquia de Criação**
- **ADMIN** → pode criar qualquer role
- **OPERATOR** → pode criar FINANCIAL, TECHNICAL, VIEWER
- **FINANCIAL/TECHNICAL** → pode criar apenas VIEWER
- **VIEWER** → não pode criar usuários
---
## 📝 Validações de Campos
### **Username**
- ✅ Obrigatório
- ✅ 3-255 caracteres
- ✅ Apenas: letras, números, ponto, hífen, underscore
- ✅ Regex: `^[a-zA-Z0-9._-]+$`
### **Password**
- ✅ Mínimo 6 caracteres
- ✅ Nunca retornado em respostas
- ✅ Sempre criptografado (BCrypt)
### **Email**
- ✅ Obrigatório
- ✅ Formato válido
- ✅ Máximo 255 caracteres
### **Role**
- ✅ Obrigatório
- ✅ Valores: ADMIN, OPERATOR, FINANCIAL, TECHNICAL, VIEWER
---
## 🔄 Fluxos de Uso
### **Criar Novo Operador**
1. Admin faz login
2. POST `/api/users` com role OPERATOR
3. Sistema valida permissões
4. Cria usuário com senha criptografada
5. Retorna dados (exceto senha)
### **Trocar Senha**
1. Usuário faz login
2. PATCH `/api/users/{id}/change-password`
3. Informa senha atual + nova senha
4. Sistema valida senha atual
5. Atualiza com nova senha criptografada
### **Resetar Senha de Usuário**
1. Admin faz login
2. PATCH `/api/users/{id}/reset-password`
3. Informa nova senha
4. Sistema atualiza diretamente (sem validar senha antiga)
### **Desativar Usuário**
1. Admin faz login
2. PATCH `/api/users/{id}/toggle-active`
3. Sistema inverte status (active ↔ inactive)
4. Usuário não consegue mais fazer login
---
## 🧪 Exemplos de Uso (cURL)
### Criar Operador
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "operador1",
    "password": "senha123",
    "email": "operador1@example.com",
    "role": "OPERATOR"
  }'
```
### Listar Usuários
```bash
curl -X GET "http://localhost:8080/api/users?page=0&size=10" \
  -H "Authorization: Bearer {token}"
```
### Trocar Senha
```bash
curl -X PATCH http://localhost:8080/api/users/5/change-password \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "senha123",
    "newPassword": "novaSenha456",
    "confirmPassword": "novaSenha456"
  }'
```
### Listar Roles
```bash
curl -X GET http://localhost:8080/api/users/roles \
  -H "Authorization: Bearer {token}"
```
---
## 🏗️ Estrutura de Arquivos
```
src/main/java/br/com/mikrotik/
├── model/
│   ├── UserRole.java              ← Enum de permissões
│   └── ApiUser.java               ← Entidade JPA
├── dto/
│   └── ApiUserDTO.java            ← DTO com validações
├── service/
│   └── ApiUserService.java        ← Lógica de negócio
├── controller/
│   └── ApiUserController.java     ← Endpoints REST
└── repository/
    └── ApiUserRepository.java     ← Acesso ao banco (já existia)
```
---
## ✅ Checklist de Implementação
- [x] Enum UserRole com hierarquia
- [x] Modelo ApiUser atualizado
- [x] ApiUserDTO com validações
- [x] ApiUserService com CRUD completo
- [x] ApiUserController com todos endpoints
- [x] Validações de permissão
- [x] Criptografia de senhas
- [x] Multi-tenant
- [x] Soft delete
- [x] Proteções de segurança
- [x] Documentação Swagger
- [x] Timestamps automáticos
- [x] Rastreamento de login
---
## 📌 Próximos Passos (Opcional)
1. [ ] Testes unitários (JUnit)
2. [ ] Testes de integração
3. [ ] Auditoria de ações (log de mudanças)
4. [ ] Exportação de lista de usuários (CSV/Excel)
5. [ ] Filtros avançados de busca
6. [ ] Histórico de alterações
7. [ ] Notificações por email (criação, reset senha)
8. [ ] Política de senhas complexas
9. [ ] Expiração de senhas
10. [ ] Bloqueio por tentativas falhas
---
## 🎓 Documentação Técnica
### **DTOs Internos**
- `ApiUserDTO.ChangePasswordDTO` - Troca de senha
- `ApiUserDTO.ResetPasswordDTO` - Reset de senha (admin)
### **Métodos do Service**
- `create()` - Criar usuário
- `findById()` - Buscar por ID
- `findByUsername()` - Buscar por username
- `findAll()` - Listar todos (paginado)
- `findAllActive()` - Listar ativos
- `findByRole()` - Listar por role
- `update()` - Atualizar usuário
- `changePassword()` - Trocar senha
- `resetPassword()` - Resetar senha
- `toggleActive()` - Ativar/desativar
- `delete()` - Soft delete
- `updateLastLogin()` - Atualizar último login
---
**Última Atualização:** 11/02/2026
**Versão:** 1.0.0
**Status:** ✅ Implementado e Funcional
