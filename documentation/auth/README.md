# 🔐 Autenticação e Gerenciamento de Usuários

## 📋 Visão Geral

Módulo responsável por autenticação JWT, gerenciamento de usuários da API e controle de permissões hierárquicas (RBAC).

---

## 📚 Documentação Disponível

| Documento | Descrição | Atualizado |
|-----------|-----------|------------|
| [USER_MANAGEMENT_API.md](./USER_MANAGEMENT_API.md) | 📘 API completa de CRUD de usuários, hierarquia de permissões, troca de senha | 2026-02-18 |

---

## 🎯 Funcionalidades Principais

### ✅ Implementado
- **Autenticação JWT** - Login com token Bearer
- **Hierarquia de Permissões** - ADMIN > OPERATOR > FINANCIAL/TECHNICAL > VIEWER
- **CRUD Completo** - Criar, listar, atualizar, desativar usuários
- **Multi-tenant** - Isolamento por empresa
- **Troca de Senha** - Usuário próprio
- **Reset de Senha** - Apenas admin
- **Soft Delete** - Desativa ao invés de excluir
- **Criptografia BCrypt** - Armazenamento seguro de senhas

### 🚧 Roadmap
- [ ] Autenticação 2FA (Two-Factor Authentication)
- [ ] OAuth2 / SSO (Single Sign-On)
- [ ] Histórico de logins com IP e dispositivo
- [ ] Bloqueio automático após tentativas falhas
- [ ] Tokens de refresh com rotação

---

## 🔗 Referências Relacionadas

**Arquitetura:**
- [../ARCHITECTURE_ACTUAL.md](../ARCHITECTURE_ACTUAL.md) - Visão geral da arquitetura
- [../REFACTORING_GUIDE.md](../REFACTORING_GUIDE.md) - Padrões arquiteturais

**Outras Features:**
- [../financial/](../financial/) - Controle de permissões financeiras
- [../contracts/](../contracts/) - Gestão de contratos

**API:**
- [../API_README.md](../API_README.md) - Documentação geral da API
- [../requests.http](../requests.http) - Exemplos de requisições

---

## 🛠️ Endpoints Principais

```
POST   /api/auth/login          - Autenticar e obter token JWT
GET    /api/users               - Listar usuários (paginado)
POST   /api/users               - Criar novo usuário
GET    /api/users/{id}          - Buscar usuário por ID
PUT    /api/users/{id}          - Atualizar usuário
DELETE /api/users/{id}          - Desativar usuário (soft delete)
PUT    /api/users/{id}/password - Trocar senha (próprio usuário)
PUT    /api/users/{id}/reset    - Reset senha (admin only)
```

---

## 📊 Diagrama de Permissões

```
┌─────────────────────────────────────────────────┐
│              ADMIN (100)                        │
│  ✅ Acesso total ao sistema                     │
│  ✅ Gerenciar todos os usuários                 │
│  ✅ Configurações globais                       │
└────────────────┬────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
┌───────▼─────────┐  ┌────▼──────────────┐
│  OPERATOR (75)  │  │  TECHNICAL (50)   │
│  Operações      │  │  Rede/Mikrotik    │
│  do dia a dia   │  │  PPPoE            │
└─────────────────┘  └───────────────────┘
        │
┌───────▼──────────┐
│ FINANCIAL (50)   │
│ Gestão Financeira│
│ Faturas/Pagtos   │
└──────────────────┘
        │
┌───────▼──────────┐
│  VIEWER (25)     │
│  Apenas leitura  │
└──────────────────┘
```

---

## 🔐 Segurança

### Regras de Negócio
- ✅ Usuário não pode deletar/desativar a si mesmo
- ✅ Apenas ADMIN pode resetar senhas de outros usuários
- ✅ Senhas devem ter no mínimo 6 caracteres
- ✅ Token JWT expira em 24 horas (configurável)
- ✅ Validação de hierarquia: role inferior não gerencia role superior

### Auditoria
- ✅ `last_login` registrado automaticamente
- ✅ `created_at` / `updated_at` para rastreamento
- ✅ Soft delete preserva histórico

---

## 🧪 Testes

```bash
# Testar login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Listar usuários (requer token)
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

Veja mais exemplos em: [../requests.http](../requests.http)

---

**📅 Última atualização:** 2026-02-18  
**👤 Responsável:** Backend Team  
**🔄 Status:** ✅ Produção

