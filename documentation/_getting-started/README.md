# ⚡ Getting Started - ISP Management API

Tudo que você precisa para começar a trabalhar no projeto.

---

## 🚀 Trilhas de Início

### 🏃 Início Rápido (5-10 minutos)
**Perfeito para:** Primeira impressão, testes rápidos, demos
- 📄 **[QUICK_START.md](QUICK_START.md)** - Setup mínimo com Docker

### 📚 Setup Completo (30-60 minutos)
**Perfeito para:** Desenvolvimento ativo, contribuições
- 📄 **[START.md](START.md)** - Instalação detalhada (Java, PostgreSQL, configurações)

### 🛠️ Guia do Desenvolvedor (referência contínua)
**Perfeito para:** Entender convenções, padrões e workflow
- 📄 **[DEVELOPMENT.md](DEVELOPMENT.md)** - Padrões de código, commit, branches, testes

---

## 🎯 Escolha Sua Trilha

### 👤 Sou um Avaliador / Recrutador
```bash
# 1. Clone o projeto
git clone <repo-url>
cd mikrotik

# 2. Suba com Docker
docker-compose up -d

# 3. Acesse a API
# Swagger: http://localhost:8080/swagger-ui.html
# Login padrão: admin / admin123
```
➡️ Leia: **[QUICK_START.md](QUICK_START.md)**

### 👨‍💻 Sou Desenvolvedor (vou contribuir)
```bash
# 1. Pré-requisitos
# - Java 21+
# - PostgreSQL 15+
# - Maven 3.9+

# 2. Clone e configure
git clone <repo-url>
cd mikrotik
cp src/main/resources/application.yml.example application.yml

# 3. Configure o banco
# Edite application.yml com suas credenciais

# 4. Execute
mvn spring-boot:run
```
➡️ Leia: **[START.md](START.md)** + **[DEVELOPMENT.md](DEVELOPMENT.md)**

### 🏢 Sou DevOps / SysAdmin
```bash
# Deploy em produção
# Ver documentos na raiz do projeto:
```
➡️ Leia: `/DEPLOY_README.md` e `/DEPLOY_OCI.md` (na raiz do projeto)

---

## 📋 Checklist de Onboarding

Marque conforme avança:

### Fase 1: Setup Básico (30 min)
- [ ] Java 21+ instalado (`java -version`)
- [ ] PostgreSQL rodando (`psql --version`)
- [ ] Maven funcionando (`mvn -version`)
- [ ] Projeto clonado e dependências baixadas (`mvn clean install`)

### Fase 2: Entendimento (1-2 horas)
- [ ] Li o [README.md principal](../../README.md)
- [ ] Entendi a [Arquitetura Atual](../architecture/ARCHITECTURE_ACTUAL.md)
- [ ] Explorei o [DOCS_INDEX.md](../DOCS_INDEX.md)
- [ ] Revisei as [Instruções do Copilot](../../.github/copilot-instructions.md)

### Fase 3: Ambiente Funcional (1 hora)
- [ ] Banco de dados criado e migrations rodadas
- [ ] Aplicação iniciou sem erros
- [ ] Consigo fazer login no Swagger UI
- [ ] Testei pelo menos 3 endpoints diferentes

### Fase 4: Primeira Contribuição (variável)
- [ ] Criei uma branch seguindo o padrão (feature/, bugfix/)
- [ ] Entendi onde fica a feature que vou trabalhar
- [ ] Li o README da feature específica
- [ ] Commitei seguindo o padrão do projeto

---

## 🆘 Problemas Comuns

### ❌ Erro de conexão com banco
```yaml
# Verifique src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/isp_management
    username: seu_usuario
    password: sua_senha
```

### ❌ Porta 8080 já em uso
```yaml
# Mude a porta em application.yml
server:
  port: 8081
```

### ❌ JWT Secret não configurado
```yaml
# Configure em application.yml
jwt:
  secret: seu-secret-aqui-minimo-256bits
  expiration: 86400000
```

### ❌ Migrations não rodam
```bash
# Force a execução
mvn flyway:migrate

# Ou limpe e recomece
mvn flyway:clean flyway:migrate
```

---

## 📖 Próximos Passos

Após o setup:

1. **Explore a API** - Use o Swagger UI ou o arquivo [`requests.http`](../requests.http)
2. **Entenda as Features** - Navegue pelas pastas em [`/documentation/`](../)
3. **Leia o Código** - Comece pela feature que você vai trabalhar
4. **Teste Localmente** - Use o script [`/test-api.sh`](../../test-api.sh)

---

## 🔗 Links Importantes

- **📚 Documentação completa:** [`/documentation/`](../)
- **🏛️ Arquitetura:** [`/documentation/architecture/`](../architecture/)
- **📘 API Reference:** [`/documentation/API_README.md`](../API_README.md)
- **🧪 Dados de Teste:** [`/documentation/shared/TEST_DATA_CPF_CNPJ.md`](../shared/TEST_DATA_CPF_CNPJ.md)

---

**Bem-vindo ao time! 🎉**

Se tiver dúvidas, abra uma issue ou consulte o [DOCS_INDEX.md](../DOCS_INDEX.md) para navegação completa.

