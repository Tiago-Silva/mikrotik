# 📚 Índice Master - Documentação ISP Management API

> **Guia completo para navegar na documentação do projeto**

---

## 🎯 Como Usar Este Índice

Este documento organiza **toda a documentação** do projeto em categorias lógicas para facilitar a navegação.

**Escolha seu perfil abaixo para ver a trilha recomendada:**
- [👨‍💻 Desenvolvedor](#-desenvolvedor)
- [👔 Gestor/PO](#-gestor--product-owner)
- [⚙️ DevOps/SysAdmin](#%EF%B8%8F-devops--sysadmin)
- [🎨 Frontend Dev](#-frontend-developer)
- [🔬 QA/Tester](#-qa--tester)
- [📊 Marketing/Business](#-marketing--business)

---

## 📂 Estrutura Organizada (Package-by-Feature)

```
documentation/
│
├── 📍 DOCUMENTAÇÃO GLOBAL (RAIZ)
│   ├── README.md               📚 Porta de entrada da documentação
│   ├── DOCS_INDEX.md           📋 Este arquivo (índice master)
│   ├── README_PTBR.md          🇧🇷 Docs em Português
│   ├── API_README.md           📘 Documentação completa da API
│   └── requests.http           🧪 Exemplos de requisições HTTP
│
├── ⚡ _GETTING-STARTED/ (Setup e Onboarding)
│   ├── README.md               📚 Guia de navegação de início
│   ├── QUICK_START.md          ⚡ Setup em 5 minutos (Docker)
│   ├── START.md                📖 Instalação detalhada
│   └── DEVELOPMENT.md          🛠️ Guia para desenvolvedores
│
├── 🏗️ ARCHITECTURE/ (Arquitetura Técnica)
│   ├── README.md               📚 Navegação arquitetural
│   ├── ARCHITECTURE_ACTUAL.md  ⭐ Arquitetura atual (DDD + Package-by-Feature)
│   ├── ARCHITECTURE.md         🏛️ Arquitetura legada (referência)
│   ├── REFACTORING_GUIDE.md    🔄 Guia de Refatoração (Hexagonal + Modular Monolith)
│   └── ROADMAP.md              🗺️ Roadmap de evolução (9 fases)
│
├── 🔧 SHARED/ (Recursos Compartilhados)
│   ├── README.md               📚 Recursos cross-feature
│   └── TEST_DATA_CPF_CNPJ.md   🧪 CPFs/CNPJs válidos para testes
│
├── 🔐 AUTH/ (Autenticação e Usuários)
│   ├── README.md               📚 Índice da feature
│   └── USER_MANAGEMENT_API.md  📘 API de gerenciamento de usuários
│
├── 👥 CUSTOMERS/ (Clientes)
│   └── README.md               📚 Índice da feature
│
├── 📝 CONTRACTS/ (Contratos e Planos)
│   └── README.md               📚 Índice da feature
│
├── 🧾 INVOICES/ (Faturamento e Cobrança)
│   ├── README.md               📚 Índice da feature
│   ├── AUTOMATIC_REACTIVATION_FLOW.md  🔄 Fluxo de reativação automática
│   ├── MANUAL_SUSPENSION_TEST.md       🧪 Testes manuais de suspensão
│   ├── TESTING_GUIDE_AUTOMATIC_SUSPENSION.md  📘 Guia de testes
│   ├── TROUBLESHOOTING_SUSPENSION.md   🚨 Solução de problemas
│   └── test-suspension.http            📝 Exemplos de requisições
│
├── 💰 FINANCIAL/ (Fluxo de Caixa)
│   ├── README.md               📚 Índice da feature
│   ├── CASH_FLOW_API_GUIDE.md  📘 Guia completo da API
│   └── BANK_ACCOUNT_TYPES.md   🏦 Tipos de contas bancárias
│
├── 🌐 NETWORK/ (Integração Mikrotik)
│   ├── README.md               📚 Índice da feature
│   └── ASYNC_INTEGRATION_GUIDE.md  📡 Guia de integrações assíncronas
│
├── 🔄 SYNC/ (Sincronização Mikrotik)
│   ├── README.md               📚 Índice da feature
│   ├── FULL_SYNC_GUIDE.md      🚀 Sincronização completa automática
│   ├── SYNC_USERS.md           🔄 Sincronização de usuários PPPoE
│   ├── SYNC_PROFILES.md        🔄 Sincronização de perfis PPPoE
│   ├── SYNC_PROFILES_IMPLEMENTATION.md  🔧 Detalhes técnicos
│   └── SYNC_FEATURE_SUMMARY.md 📝 Resumo de funcionalidades
│
└── 📦 _ARCHIVE/ (Documentos Históricos)
    ├── README.md               📚 Explicação do arquivo
    ├── IMPLEMENTATION_COMPLETE.md  ✅ Status de implementação (MVP)
    ├── CHECKLIST.md            ☑️ Checklist de funcionalidades
    ├── CONCLUSION.md           🎊 Conclusão de fase
    ├── SUMMARY.md              📊 Sumário executivo histórico
    ├── MANIFEST.md             📋 Manifesto de arquivos
    ├── INDEX_COMPLETE.md       📑 Índice completo antigo
    ├── INDEX.md                📚 Índice original
    ├── ORGANIZATION_SUMMARY.md 📋 Resumo de reorganização
    ├── REORGANIZATION_SUMMARY.md 📋 Outro resumo de reorganização
    ├── STRUCTURE.md            📢 Posts para LinkedIn
    ├── MIGRATION_GUIDE.md      🔄 Guia de migração antigo
    ├── COMMIT_MESSAGE.txt      📝 Mensagem de commit vazia
    └── verify-implementation.sh 🔍 Script de verificação antigo
```

---

## 👨‍💻 Desenvolvedor

### 🎯 Trilha de Onboarding (3-4 horas)

**Dia 1 - Entendendo o Projeto (1h)**
1. 📖 [../README.md](../README.md) - Visão geral (15min)
2. 🏛️ [architecture/ARCHITECTURE_ACTUAL.md](architecture/ARCHITECTURE_ACTUAL.md) - Arquitetura Package-by-Feature (20min)
3. 🔄 [architecture/REFACTORING_GUIDE.md](architecture/REFACTORING_GUIDE.md) - Padrões e proteções (15min)
4. 🗺️ [architecture/ROADMAP.md](architecture/ROADMAP.md) - Roadmap de evolução (10min)

**Dia 1 - Setup do Ambiente (30min)**
5. ⚡ [_getting-started/QUICK_START.md](_getting-started/QUICK_START.md) - Configuração rápida (20min)
6. 🛠️ [_getting-started/DEVELOPMENT.md](_getting-started/DEVELOPMENT.md) - Padrões de código (10min)

**Dia 2 - Explorando Features (1-2h)**
7. 📘 [API_README.md](API_README.md) - Endpoints completos (30min)
8. 🧪 [requests.http](requests.http) - Testar requisições (20min)
9. 🔐 [auth/README.md](auth/README.md) - Autenticação e usuários (15min)
10. 🌐 [network/README.md](network/README.md) - Integração Mikrotik (20min)
11. 🔄 [sync/README.md](sync/README.md) - Sincronização (15min)

**Dia 3 - Features de Negócio (1h)**
12. 👥 [customers/README.md](customers/README.md) - Clientes (10min)
13. 📝 [contracts/README.md](contracts/README.md) - Contratos (15min)
14. 🧾 [invoices/README.md](invoices/README.md) - Faturamento (20min)
15. 💰 [financial/README.md](financial/README.md) - Fluxo de caixa (15min)

### 📚 Referência Rápida por Feature

| Feature | README | Documentação Principal |
|---------|--------|------------------------|
| 🔐 Autenticação | [auth/README.md](auth/README.md) | [USER_MANAGEMENT_API.md](auth/USER_MANAGEMENT_API.md) |
| 👥 Clientes | [customers/README.md](customers/README.md) | [shared/TEST_DATA_CPF_CNPJ.md](shared/TEST_DATA_CPF_CNPJ.md) |
| 📝 Contratos | [contracts/README.md](contracts/README.md) | Em desenvolvimento |
| 🧾 Faturamento | [invoices/README.md](invoices/README.md) | [AUTOMATIC_REACTIVATION_FLOW.md](invoices/AUTOMATIC_REACTIVATION_FLOW.md) |
| 💰 Financeiro | [financial/README.md](financial/README.md) | [CASH_FLOW_API_GUIDE.md](financial/CASH_FLOW_API_GUIDE.md) |
| 🌐 Rede/Mikrotik | [network/README.md](network/README.md) | [ASYNC_INTEGRATION_GUIDE.md](network/ASYNC_INTEGRATION_GUIDE.md) |
| 🔄 Sincronização | [sync/README.md](sync/README.md) | [FULL_SYNC_GUIDE.md](sync/FULL_SYNC_GUIDE.md) |

### 📚 Referência Rápida Geral

| Preciso... | Veja... |
|------------|---------|
| Criar novo endpoint | [_getting-started/DEVELOPMENT.md](_getting-started/DEVELOPMENT.md) + [API_README.md](API_README.md) |
| Entender arquitetura | [architecture/ARCHITECTURE_ACTUAL.md](architecture/ARCHITECTURE_ACTUAL.md) |
| Adicionar autenticação | [auth/README.md](auth/README.md) |
| Integrar com Mikrotik | [network/ASYNC_INTEGRATION_GUIDE.md](network/ASYNC_INTEGRATION_GUIDE.md) |
| Trabalhar com banco | [../src/main/resources/db/schema.sql](../src/main/resources/db/schema.sql) |
| Testar API | [requests.http](requests.http) |
| Ver padrões de código | [_getting-started/DEVELOPMENT.md](_getting-started/DEVELOPMENT.md) |

---

## 👔 Gestor / Product Owner

### 🎯 Trilha Executiva (30-45 min)

**Visão Geral (15min)**
1. 📖 [../README.md](../README.md) - Overview do projeto
2. 🏛️ [architecture/ARCHITECTURE_ACTUAL.md](architecture/ARCHITECTURE_ACTUAL.md) - Visão técnica de alto nível

**Planejamento (20min)**
3. 🗺️ [architecture/ROADMAP.md](architecture/ROADMAP.md) - Roadmap de 9 fases
4. 📘 [API_README.md](API_README.md) - Features disponíveis (endpoints)

**Documentação Histórica (10min)**
5. ✅ [_archive/IMPLEMENTATION_COMPLETE.md](_archive/IMPLEMENTATION_COMPLETE.md) - Status do MVP original
6. 🎊 [_archive/CONCLUSION.md](_archive/CONCLUSION.md) - Conclusão e próximos passos

### 📊 Métricas Rápidas

- **Controllers:** 13+ (70+ endpoints REST)
- **Services:** 15+ classes de negócio
- **Models:** 15+ entidades
- **Documentação:** 15.000+ linhas
- **Código:** 8.500+ linhas
- **Status:** ✅ Pronto para produção

---

## ⚙️ DevOps / SysAdmin

### 🎯 Trilha de Deploy (1-2h)

**Instalação (30min)**
1. 📖 [_getting-started/START.md](_getting-started/START.md) - Instalação detalhada
2. 💾 [../src/main/resources/db/schema.sql](../src/main/resources/db/schema.sql) - Banco de dados
3. 🐳 [../docker-compose.yml](../docker-compose.yml) - Docker

**Arquitetura (20min)**
4. 🏛️ [architecture/ARCHITECTURE_ACTUAL.md](architecture/ARCHITECTURE_ACTUAL.md) - Infraestrutura
5. 🔄 [architecture/REFACTORING_GUIDE.md](architecture/REFACTORING_GUIDE.md) - Padrões técnicos

**Configuração (30min)**
6. 🔧 [../src/main/resources/application.yml](../src/main/resources/application.yml) - Configs
7. 📘 [API_README.md](API_README.md) - Documentação de endpoints

**Validação (10min)**
8. 🔍 [verify-implementation.sh](verify-implementation.sh) - Script de verificação

### 🔧 Checklist de Deploy

- [ ] MySQL 8.0+ instalado
- [ ] Java 21+ instalado
- [ ] Portas 8080, 3306 liberadas
- [ ] `.env` configurado
- [ ] `jwt.secret` alterado
- [ ] SSL/HTTPS configurado
- [ ] Firewall configurado
- [ ] Backups agendados

---

## 🎨 Frontend Developer

### 🎯 Trilha de Integração (1-2h)

**Entendendo a API (45min)**
1. 📘 [API_README.md](API_README.md) - Todos os endpoints
2. 🧪 [requests.http](requests.http) - Exemplos práticos
3. 🌐 Swagger UI: `http://localhost:8080/swagger-ui.html`

**Funcionalidades Especiais (30min)**
4. 🔄 [SYNC_USERS.md](SYNC_USERS.md) - Sincronização de usuários
5. 🔄 [SYNC_PROFILES.md](SYNC_PROFILES.md) - Sincronização de perfis

**Testando (15min)**
6. 🧪 [../test-api.sh](../test-api.sh) - Scripts de teste

### 📡 Endpoints Principais

| Módulo | Endpoint Base | Docs |
|--------|---------------|------|
| Auth | `/api/auth` | [API_README.md](API_README.md#autenticação) |
| Empresas | `/api/companies` | [API_README.md](API_README.md#empresas) |
| Clientes | `/api/customers` | [API_README.md](API_README.md#clientes) |
| Servidores | `/api/mikrotik-servers` | [API_README.md](API_README.md#servidores) |
| Perfis | `/api/profiles` | [API_README.md](API_README.md#perfis) |
| Usuários PPPoE | `/api/users` | [API_README.md](API_README.md#usuários) |
| Contratos | `/api/contracts` | [API_README.md](API_README.md#contratos) |
| Faturas | `/api/invoices` | [API_README.md](API_README.md#faturas) |

---

## 🔬 QA / Tester

### 🎯 Trilha de Testes (2-3h)

**Setup (30min)**
1. ⚡ [QUICK_START.md](QUICK_START.md) - Ambiente
2. 📘 [API_README.md](API_README.md) - Endpoints

**Testando (2h)**
3. 🧪 [requests.http](requests.http) - Casos de teste HTTP
4. 🧪 [../test-api.sh](../test-api.sh) - Scripts automatizados
5. ☑️ [CHECKLIST.md](CHECKLIST.md) - Funcionalidades para testar

**Validação (30min)**
6. ✅ [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md) - Status
7. 🔍 [verify-implementation.sh](verify-implementation.sh) - Verificação

### 🧪 Casos de Teste

| Módulo | Testes | Arquivo |
|--------|--------|---------|
| Autenticação | Login, Token, Roles | [requests.http](requests.http#L1-L20) |
| CRUD Básico | Create, Read, Update, Delete | [requests.http](requests.http#L21-L100) |
| Sincronização | Sync Users, Sync Profiles | [SYNC_USERS.md](SYNC_USERS.md) |
| Validações | CPF, CNPJ, Email | [API_README.md](API_README.md) |

---

## 📊 Marketing / Business

### 🎯 Trilha de Divulgação (20-30min)

**Entendendo o Projeto (10min)**
1. 📖 [../README.md](../README.md) - Visão geral e benefícios
2. 📊 [SUMMARY.md](SUMMARY.md) - Sumário executivo

**Material Pronto (15min)**
3. 💼 [LINKEDIN_POST.md](LINKEDIN_POST.md) - 4 versões de posts
4. 🏗️ Hierarquia de entidades visual

**Estatísticas (5min)**
5. 📋 [MANIFEST.md](MANIFEST.md) - Números do projeto

### 📢 Material de Divulgação

| Tipo | Onde Encontrar |
|------|----------------|
| Post LinkedIn (Versão Principal) | [LINKEDIN_POST.md](LINKEDIN_POST.md#versão-principal) |
| Post LinkedIn (Versão Curta) | [LINKEDIN_POST.md](LINKEDIN_POST.md#versão-curta) |
| Post Técnico | [LINKEDIN_POST.md](LINKEDIN_POST.md#versão-técnica) |
| Post com Resultados | [LINKEDIN_POST.md](LINKEDIN_POST.md#versão-com-resultados) |
| Hierarquia Visual | [LINKEDIN_POST.md](LINKEDIN_POST.md#hierarquia-visual) |
| Hashtags Sugeridas | [LINKEDIN_POST.md](LINKEDIN_POST.md#hashtags) |

---

## 🔍 Busca Rápida por Tema

### Autenticação & Segurança
- [DEVELOPMENT.md](DEVELOPMENT.md) (seção JWT)
- [API_README.md](API_README.md) (endpoints `/api/auth`)
- [ARCHITECTURE.md](ARCHITECTURE.md) (camada de segurança)

### Multi-tenant
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [schema.sql](schema.sql) (tabela `companies`)
- [API_README.md](API_README.md) (endpoints `/api/companies`)

### Sincronização MikroTik
- [SYNC_USERS.md](SYNC_USERS.md)
- [SYNC_PROFILES.md](SYNC_PROFILES.md)
- [SYNC_PROFILES_IMPLEMENTATION.md](SYNC_PROFILES_IMPLEMENTATION.md)
- [SYNC_FEATURE_SUMMARY.md](SYNC_FEATURE_SUMMARY.md)

### Contratos & Financeiro
- [ROADMAP.md](ROADMAP.md) (Fase 4)
- [schema.sql](schema.sql) (tabelas `contracts`, `invoices`, `transactions`)
- [API_README.md](API_README.md) (endpoints financeiros)

### Banco de Dados
- [schema.sql](schema.sql)
- [START.md](START.md) (seção configuração)
- [ARCHITECTURE.md](ARCHITECTURE.md) (modelo de dados)

---

## 📈 Estatísticas da Documentação

- **Total de Arquivos:** 22
- **Markdown:** 19 arquivos
- **Linhas de Documentação:** ~15.000+
- **Guias de Setup:** 2 (Quick + Completo)
- **Guias de Desenvolvimento:** 1
- **Documentação de API:** 1 (completa)
- **Scripts de Teste:** 2
- **Schemas SQL:** 1
- **Idiomas:** 2 (PT-BR, EN)

---

## 🎯 Prioridades por Urgência

### 🔥 Crítico (Leia Primeiro)
1. [QUICK_START.md](QUICK_START.md) - Para começar
2. [API_README.md](API_README.md) - Para usar a API
3. [DEVELOPMENT.md](DEVELOPMENT.md) - Para desenvolver

### ⚠️ Importante
4. [ARCHITECTURE.md](ARCHITECTURE.md) - Para entender o sistema
5. [schema.sql](schema.sql) - Para trabalhar com dados
6. [ROADMAP.md](ROADMAP.md) - Para planejar

### ℹ️ Complementar
7. [SUMMARY.md](SUMMARY.md) - Visão executiva
8. [CHECKLIST.md](CHECKLIST.md) - Validação
9. [CONCLUSION.md](CONCLUSION.md) - Próximos passos

---

## 🤝 Contribuindo

Antes de contribuir, leia:

1. [../README.md](../README.md#como-contribuir)
2. [DEVELOPMENT.md](DEVELOPMENT.md)
3. [ARCHITECTURE.md](ARCHITECTURE.md)

---

## 📞 Suporte

- 📧 **Issues:** [GitHub Issues](https://github.com/seu-usuario/mikrotik/issues)
- 💬 **Discussões:** [GitHub Discussions](https://github.com/seu-usuario/mikrotik/discussions)
- 📖 **Wiki:** [GitHub Wiki](https://github.com/seu-usuario/mikrotik/wiki)
- 🌐 **Swagger:** `http://localhost:8080/swagger-ui.html`

---

## 🏷️ Tags

`#documentation` `#api` `#mikrotik` `#isp` `#spring-boot` `#rest-api` `#multi-tenant` `#pppoe` `#java` `#mysql`

---

**📅 Última Atualização:** Janeiro 2026  
**👤 Mantido por:** Tiago Almeida  
**📄 Licença:** MIT  
**✅ Status:** Documentação Completa e Organizada
