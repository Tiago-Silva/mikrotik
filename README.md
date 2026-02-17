# 🚀✨ API REST Completa para Gerenciamento de Provedores de Internet (ISP) ✨

> ## 🏗️ **ARQUITETURA ATUAL: MODULAR MONOLITH + ASYNC EVENTS**
> 
> **Status:** ✅ Em Operação | 🚀 Integrações Assíncronas Implementadas
> 
> Este projeto utiliza uma arquitetura **Modular Monolith organizada por Features**, com proteção transacional via **Eventos e Processamento Assíncrono**.
> 
> 📘 **Documentação Arquitetural:**
> - [ARCHITECTURE_ACTUAL.md](documentation/ARCHITECTURE_ACTUAL.md) - Arquitetura real e decisões (ADRs)
> - [ASYNC_INTEGRATION_GUIDE.md](documentation/ASYNC_INTEGRATION_GUIDE.md) - Guia de integrações assíncronas
> - [documentation/REFACTORING_GUIDE.md](documentation/REFACTORING_GUIDE.md) - Histórico de refatoração
>
> 🎯 **Benefícios Chave:**
> - **API Rápida (<500ms):** Operações pesadas (Mikrotik) rodam em background
> - **Resiliência:** Retry automático (3x) em falhas de rede
> - **Organização:** Código separado por domínios (features)

## 🌐 Sobre o Projeto

**De gerenciador de PPPoE para ERP completo de ISP!**

Este projeto começou como uma solução para automatizar o gerenciamento de servidores MikroTik e evoluiu para um **sistema completo de gestão de provedores de internet**, integrando camadas técnica, comercial e financeira em uma única plataforma escalável.

### 🎯 Visão Atual

Esta API REST robusta permite que provedores de internet (ISPs) automatizem completamente suas operações:
- 🔧 **Camada Técnica**: Gestão de servidores MikroTik, perfis PPPoE, usuários e conexões
- 👥 **Camada CRM**: Gestão de clientes (PF/PJ), endereços e geolocalização
- 💼 **Camada Comercial**: Planos comerciais, contratos e lifecycle management
- 💰 **Camada Financeira**: Faturas automáticas, pagamentos e integração com gateways
- 🤖 **Automação**: Régua de cobrança, bloqueios automáticos e notificações

### ⚡ Integrações Assíncronas (Novo)

O sistema agora utiliza processamento em background para comunicação com hardware Mikrotik:

*   **O que mudou?** Suspensão e ativação retornam instantaneamente; processamento ocorre em thread separada.
*   **Como monitorar?** Acompanhe logs com tag `[network-integration-1]`.
*   **Guia Completo**: [documentation/ASYNC_INTEGRATION_GUIDE.md](documentation/ASYNC_INTEGRATION_GUIDE.md)

### 📊 Status do Projeto

- ✅ **Fase 1 (Concluída)**: PPPoE Management System
- 🔍 **Fase 2 (Em revisão)**: Multi-tenant + CRM - *Código implementado, em processo de revisão*
- 🔍 **Fases 3-5 (Em revisão)**: Comercial + Financeiro + Automação - *Código implementado, em processo de revisão*

> 💡 **Nota:** As fases 2-5 estão com código implementado e funcional, mas passando por processo de code review, refatoração e otimização antes da versão final.

📖 **Veja o roadmap completo**: [documentation/ROADMAP.md](documentation/ROADMAP.md)

## ⭐ Como Colaborar com o Projeto

> 🎉 **Obrigado pelas 53 clones!** Agora queremos que você faça parte da comunidade!

Este projeto é **open source** e sua contribuição é muito bem-vinda! Mas antes de clonar, considere fazer um **FORK** do repositório:

### 🍴 Por que fazer Fork?

1. **🔔 Receba atualizações**: Você será notificado sobre novas features e correções
2. **🤝 Contribua facilmente**: Faça suas melhorias e envie Pull Requests
3. **📊 Mostre seu interesse**: Ajuda o projeto a crescer e ganhar visibilidade
4. **💡 Personalize**: Mantenha suas customizações sincronizadas com a versão oficial

### 📝 Como Fazer Fork e Contribuir

```bash
# 1. Clique no botão "Fork" no topo desta página (GitHub)

# 2. Clone SEU fork (não o repositório original)
git clone https://github.com/SEU-USUARIO/mikrotik.git
cd mikrotik

# 3. Adicione o repositório original como remote
git remote add upstream https://github.com/USUARIO-ORIGINAL/mikrotik.git

# 4. Crie uma branch para sua feature
git checkout -b minha-contribuicao

# 5. Faça suas alterações e commit
git add .
git commit -m "feat: minha contribuição incrível"

# 6. Envie para SEU fork
git push origin minha-contribuicao

# 7. Abra um Pull Request no GitHub!
```

### 🎯 Formas de Contribuir

- 🐛 **Reportar bugs**: Abra uma [issue](https://github.com/seu-usuario/mikrotik/issues)
- 💡 **Sugerir features**: Compartilhe suas ideias conosco
- 📝 **Melhorar documentação**: Corrija erros ou adicione exemplos
- 🔧 **Enviar código**: Implemente features do roadmap ou corrija bugs
- ⭐ **Dar estrela**: Isso nos motiva muito!

### 🏆 Seja Reconhecido!

Todos os contribuidores serão creditados no projeto. Junte-se a nós! 💪

---

## 🚀 Funcionalidades Principais

### ✅ Fase 1: PPPoE Management (Implementado)

✅ **Autenticação JWT Segura**: Login com tokens JWT e controle de acesso baseado em roles (ADMIN, OPERATOR, VIEWER).  
✅ **Gerenciamento Multi-Servidor**: Configure e gerencie múltiplos servidores MikroTik a partir de uma única API.  
✅ **Perfis PPPoE Personalizados**: Crie perfis com limites de banda (download/upload), timeouts e configurações específicas.  
✅ **Gestão Completa de Usuários**: Crie, edite, ative, desative e delete usuários PPPoE diretamente no MikroTik via SSH.  
✅ **Sincronização de Usuários**: Importe automaticamente todos os usuários PPPoE já existentes no MikroTik para o banco de dados.  
✅ **Sincronização de Perfis**: Importe automaticamente todos os perfis PPPoE já existentes no MikroTik para o banco de dados.  
✅ **Monitoramento de Conexões**: Visualize conexões ativas, estatísticas de tráfego e histórico de sessões.  
✅ **Auditoria Completa**: Registro automático de todas as operações para rastreabilidade e compliance.  
✅ **Documentação Interativa**: Swagger UI integrado para testar endpoints sem escrever código.  
✅ **Processamento Eficiente**: Paginação, filtros e otimizações para lidar com grandes volumes de dados.

### 🔨 Fase 2-5: ISP Management (Em revisão)

🔨 **Multi-tenant**: Suporte a múltiplas empresas na mesma infraestrutura  
🔨 **CRM Completo**: Gestão de clientes (PF/PJ), documentos, endereços com geolocalização  
🔨 **Planos Comerciais**: Separação entre perfis técnicos e planos de venda  
🔨 **Contratos**: Gestão de lifecycle (DRAFT → ACTIVE → SUSPENDED → CANCELED)  
🔨 **Financeiro**: Faturas automáticas, cálculo de juros/multa, integração com gateways (Asaas/Juno)  
🔨 **Régua de Cobrança**: Automação completa (lembretes, redução de velocidade, bloqueios)  
🔨 **Notificações**: E-mail e SMS automáticos (SendGrid, Twilio)  
🔨 **Dashboards**: Métricas financeiras, técnicas e operacionais  

📖 **Documentação completa**: [documentation/ISP_EXPANSION_PLAN.md](documentation/ISP_EXPANSION_PLAN.md)

## 🚀 Instalando

### Pré-requisitos

Antes de começar, certifique-se de ter instalado:
- **Java 21+** ☕
- **Maven 3.8+** 📦
- **Docker & Docker Compose** 🐳 (opcional, mas recomendado)
- **MySQL 8.0+** 🗄️
- **Servidor MikroTik** com SSH habilitado 📡

### 1️⃣ Clone o Projeto

```bash
git clone https://github.com/Tiago-Silva/mikrotik.git
cd mikrotik
```

### 2️⃣ Configure o Banco de Dados

**Opção A: Usando Docker (Recomendado) 🐳**

O projeto já vem com um `docker-compose.yml` configurado com MySQL e phpMyAdmin:

```bash
docker-compose up -d
```

Isso iniciará:
- **MySQL** em `localhost:3306` (usuário: `root`, senha: `root`)
- **phpMyAdmin** em `http://localhost:8081`

**Opção B: MySQL Manual**

```sql
CREATE DATABASE mikrotik_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3️⃣ Configure as Variáveis de Ambiente

Edite o arquivo `src/main/resources/application.yml` com suas configurações:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mikrotik_db?useSSL=false&serverTimezone=UTC
    username: root
    password: root

jwt:
  secret: sua-chave-secreta-super-segura-aqui
  expiration: 86400000
```

### 4️⃣ Compile e Execute

```bash
# Compilar o projeto
mvn clean install

# Executar a aplicação
mvn spring-boot:run
```

🎉 **Pronto!** A API estará rodando em `http://localhost:8080`

### 5️⃣ Acesse a Documentação Interativa

Abra seu navegador e acesse:

```
http://localhost:8080/swagger-ui.html
```

## 🚀⚙️ Como Funciona? (O Fluxo Técnico)

A aplicação segue uma arquitetura em camadas bem definida:

```
Cliente (Swagger/Postman/App)
        ↓
REST Controllers (Endpoints HTTP)
        ↓
Security Layer (JWT Authentication)
        ↓
Service Layer (Lógica de Negócio)
        ↓
Repository Layer (Acesso a Dados)
        ↓
Database (MySQL) + MikroTik (SSH)
```

### 🔐 Autenticação

1. **Login**: Envie credenciais para `/api/auth/login`
2. **Token**: Receba um JWT token válido por 24h
3. **Uso**: Inclua o token no header `Authorization: Bearer <token>` em todas as requisições

### 📡 Comunicação com MikroTik

A API se conecta aos servidores MikroTik via **SSH (JSch)** para executar comandos RouterOS, permitindo:
- Criar/editar/deletar usuários PPPoE
- Configurar perfis de banda
- Monitorar conexões ativas em tempo real

## 🚀🗂️ Principais Endpoints da API

### 🔒 Autenticação
```
POST   /api/auth/login              - Login de usuário
GET    /api/auth/validate           - Validar token JWT
```

### 🖥️ Servidores MikroTik
```
POST   /api/mikrotik-servers        - Adicionar novo servidor
GET    /api/mikrotik-servers        - Listar todos os servidores
GET    /api/mikrotik-servers/{id}   - Obter servidor específico
PUT    /api/mikrotik-servers/{id}   - Atualizar servidor
DELETE /api/mikrotik-servers/{id}   - Remover servidor
POST   /api/mikrotik-servers/{id}/test-connection - Testar conexão SSH
```

### 📊 Perfis PPPoE
```
POST   /api/profiles                - Criar novo perfil
GET    /api/profiles                - Listar todos os perfis
GET    /api/profiles/{id}           - Obter perfil específico
PUT    /api/profiles/{id}           - Atualizar perfil
DELETE /api/profiles/{id}           - Deletar perfil
```

### 👥 Usuários PPPoE
```
POST   /api/users                   - Criar novo usuário
GET    /api/users                   - Listar todos os usuários
GET    /api/users/{id}              - Obter usuário específico
PUT    /api/users/{id}              - Atualizar usuário
DELETE /api/users/{id}              - Deletar usuário
POST   /api/users/{id}/disable      - Desativar usuário
POST   /api/users/{id}/enable       - Ativar usuário
```

### 📈 Conexões e Monitoramento
```
GET    /api/connections                     - Listar todas as conexões
GET    /api/connections/active/count        - Contar conexões ativas
GET    /api/connections/server/{serverId}/active - Conexões ativas por servidor
```

## 🚀👥 Usuários Padrão

A aplicação cria automaticamente 3 usuários de teste no primeiro boot:

| Usuário   | Senha        | Role     | Permissões                          |
|-----------|--------------|----------|-------------------------------------|
| `admin`   | `admin123`   | ADMIN    | Acesso total (CRUD em tudo)        |
| `operator`| `operator123`| OPERATOR | Gerenciar perfis e usuários        |
| `viewer`  | `viewer123`  | VIEWER   | Apenas visualização (somente leitura) |

## 🚀🛠️ Tecnologias Utilizadas

<p align="left">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=java,spring,mysql,maven,docker,git,github,linux" />
  </a>
</p>

### Backend & Frameworks:
- **Java 21** - Linguagem de programação moderna e robusta
- **Spring Boot 4.0.1** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Segurança e autenticação
- **Spring Validation** - Validação de dados

### Segurança:
- **JWT (JSON Web Token)** - Autenticação stateless
- **jjwt 0.11.5** - Biblioteca para geração e validação de tokens

### Banco de Dados:
- **MySQL 8.0** - Banco de dados relacional
- **Hibernate** - ORM (Object-Relational Mapping)

### Integração MikroTik:
- **JSch 0.1.55** - Biblioteca SSH para comunicação com RouterOS

### Documentação:
- **SpringDoc OpenAPI** - Documentação automática da API
- **Swagger UI** - Interface interativa para testes

### DevOps:
- **Docker & Docker Compose** - Containerização
- **Maven** - Gerenciamento de dependências e build
- **Lombok** - Redução de boilerplate

## 🚀📖 Documentação Adicional

Este projeto possui documentação detalhada na pasta `/documentation`:

- **[API_README.md](documentation/API_README.md)** - Documentação completa dos endpoints
- **[QUICK_START.md](documentation/QUICK_START.md)** - Guia rápido de 5 minutos
- **[ARCHITECTURE.md](documentation/ARCHITECTURE.md)** - Arquitetura detalhada do sistema
- **[DEVELOPMENT.md](documentation/DEVELOPMENT.md)** - Guia para desenvolvedores
- **[requests.http](documentation/requests.http)** - Exemplos de requisições HTTP

## 🚀🧪 Testando a API

### Via Swagger UI (Mais Fácil)

1. Acesse: `http://localhost:8080/swagger-ui.html`
2. Expanda o endpoint `/api/auth/login`
3. Clique em "Try it out"
4. Use as credenciais: `admin` / `admin123`
5. Copie o token retornado
6. Clique em "Authorize" (cadeado no topo)
7. Cole o token e explore os outros endpoints!

### Via cURL

```bash
# 1. Fazer login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Usar o token retornado
TOKEN="seu-token-aqui"

# 3. Listar servidores
curl -X GET http://localhost:8080/api/mikrotik-servers \
  -H "Authorization: Bearer $TOKEN"
```

## 🚀📝 Exemplo de Uso Completo

**Cenário**: Adicionar um novo servidor MikroTik e criar um usuário PPPoE.

### 1. Fazer Login
```bash
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

### 2. Adicionar Servidor MikroTik
```bash
POST /api/mikrotik-servers
{
  "name": "Servidor Central",
  "host": "192.168.1.1",
  "port": 22,
  "username": "admin",
  "password": "mikrotik123",
  "isActive": true
}
```

### 3. Criar Perfil de Banda
```bash
POST /api/profiles
{
  "name": "10MB",
  "serverId": 1,
  "downloadSpeed": "10M",
  "uploadSpeed": "5M",
  "sessionTimeout": 0,
  "isActive": true
}
```

### 4. Criar Usuário PPPoE
```bash
POST /api/users
{
  "username": "cliente001",
  "password": "senha123",
  "serverId": 1,
  "profileId": 1,
  "email": "cliente@email.com",
  "comment": "Cliente residencial",
  "isActive": true
}
```

### 5. Monitorar Conexões Ativas
```bash
GET /api/connections/server/1/active
```

## 🚀🔒 Segurança

- ✅ Autenticação JWT com expiração configurável
- ✅ Senhas criptografadas com BCrypt
- ✅ Controle de acesso baseado em roles (RBAC)
- ✅ Validação de entrada em todos os endpoints
- ✅ Proteção contra SQL Injection (JPA)
- ✅ CORS configurável
- ✅ Auditoria de todas as operações

## 🚀🐳 Deploy com Docker

Para fazer deploy da aplicação completa (API + MySQL):

```bash
# Build da aplicação
mvn clean package -DskipTests

# Criar imagem Docker (adicione um Dockerfile se necessário)
docker build -t mikrotik-api .

# Subir toda a stack
docker-compose up -d
```

## 🚀📊 Roadmap Futuro

- [ ] Dashboard web interativo
- [ ] Relatórios de consumo e faturamento
- [ ] Notificações por webhook/email
- [ ] Suporte a GraphQL
- [ ] Backup automático de configurações
- [ ] Integração com sistemas de billing
- [ ] App mobile (Android/iOS)

## 🚀🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/NovaFuncionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/NovaFuncionalidade`)
5. Abra um Pull Request

## 🚀📜 Licença

Este projeto é distribuído sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 🚀💬 Suporte

Encontrou um bug? Tem uma sugestão? Abra uma [issue](https://github.com/seu-usuario/mikrotik/issues)!

---

**Feito com ❤️, Java e muito Spring Boot!** ☕🚀
