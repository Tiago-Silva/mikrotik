# 📚 Documentação - ISP Management API

> **Bem-vindo à documentação completa do projeto!**

---

## 🆕 Nova Organização (2026-02-18)

**A documentação foi totalmente reorganizada!** 🎉

Agora os documentos estão organizados de forma **hierárquica e lógica**, seguindo a arquitetura **Package-by-Feature** do código:

```
documentation/
├── 📍 Raiz (entry-points)        - README, DOCS_INDEX, API_README
├── ⚡ _getting-started/          - Setup e onboarding
├── 🏗️ architecture/              - Documentação técnica/arquitetural
├── 🔧 shared/                    - Recursos compartilhados
├── 📦 _archive/                  - Documentos históricos
│
├── 🔐 auth/                      - Autenticação e usuários
├── 👥 customers/                 - Gestão de clientes  
├── 📝 contracts/                 - Contratos e planos
├── 🧾 invoices/                  - Faturamento e cobrança
├── 💰 financial/                 - Fluxo de caixa
├── 🌐 network/                   - Integração Mikrotik
└── 🔄 sync/                      - Sincronização
```

Cada pasta tem seu próprio `README.md` como ponto de entrada.

---

## 🚀 Início Rápido

**Novo no projeto?** Siga esta sequência:

1. **[_getting-started/QUICK_START.md](_getting-started/QUICK_START.md)** - Configure o ambiente em 5 minutos ⚡
2. **[DOCS_INDEX.md](DOCS_INDEX.md)** - Índice master por features 📋
3. **[API_README.md](API_README.md)** - Explore os endpoints da API 📘
4. **[requests.http](requests.http)** - Teste requisições práticas 🧪

---

## 🎯 Navegação por Feature

| Feature | README | Documentação Principal |
|---------|--------|------------------------|
| 🔐 Autenticação | [auth/README.md](auth/README.md) | JWT, usuários, permissões |
| 👥 Clientes | [customers/README.md](customers/README.md) | Cadastro PF/PJ |
| 📝 Contratos | [contracts/README.md](contracts/README.md) | Planos, status |
| 🧾 Faturamento | [invoices/README.md](invoices/README.md) | Suspensão, reativação |
| 💰 Financeiro | [financial/README.md](financial/README.md) | Fluxo de caixa |
| 🌐 Rede/Mikrotik | [network/README.md](network/README.md) | Integração assíncrona |
| 🔄 Sincronização | [sync/README.md](sync/README.md) | Import de PPPoE |

---

## 📖 Navegação por Perfil

Escolha seu perfil para ver a documentação mais relevante:

### 👨‍💻 [Desenvolvedor](DOCS_INDEX.md#-desenvolvedor)
- Trilha de onboarding (3-4h)
- Padrões de código
- Exemplos práticos

### 👔 [Gestor / Product Owner](DOCS_INDEX.md#-gestor--product-owner)
- Visão executiva (30min)
- Roadmap e planejamento
- Status de implementação

### ⚙️ [DevOps / SysAdmin](DOCS_INDEX.md#%EF%B8%8F-devops--sysadmin)
- Guia de deploy
- Configuração de infraestrutura
- Scripts de verificação

### 🎨 [Frontend Developer](DOCS_INDEX.md#-frontend-developer)
- Integração com a API
- Endpoints e DTOs
- Exemplos de consumo

### 🔬 [QA / Tester](DOCS_INDEX.md#-qa--tester)
- Casos de teste
- Scripts automatizados
- Checklist de funcionalidades

### 📊 [Marketing / Business](DOCS_INDEX.md#-marketing--business)
- Material de divulgação
- Posts para LinkedIn
- Estatísticas do projeto

---

## 📂 Estrutura da Documentação

### ⚡ Início Rápido ([_getting-started/](_getting-started/))
- **[QUICK_START.md](_getting-started/QUICK_START.md)** - Setup em 5 minutos
- **[START.md](_getting-started/START.md)** - Instalação detalhada
- **[DEVELOPMENT.md](_getting-started/DEVELOPMENT.md)** - Guia do desenvolvedor

### 🏗️ Arquitetura ([architecture/](architecture/))
- **[ARCHITECTURE_ACTUAL.md](architecture/ARCHITECTURE_ACTUAL.md)** - Arquitetura atual ⭐
- **[ARCHITECTURE.md](architecture/ARCHITECTURE.md)** - Arquitetura legada (referência)
- **[REFACTORING_GUIDE.md](architecture/REFACTORING_GUIDE.md)** - Guia de refatoração
- **[ROADMAP.md](architecture/ROADMAP.md)** - Roadmap (9 fases)

### 🔧 Recursos Compartilhados ([shared/](shared/))
- **[TEST_DATA_CPF_CNPJ.md](shared/TEST_DATA_CPF_CNPJ.md)** - Dados de teste válidos

### 📡 API & Desenvolvimento
- **[API_README.md](API_README.md)** - Documentação completa dos 70+ endpoints
- **[DEVELOPMENT.md](DEVELOPMENT.md)** - Guia para desenvolvedores
- **[requests.http](requests.http)** - Exemplos HTTP
- **[INDEX.md](INDEX.md)** - Índice de recursos

### ✨ Funcionalidades
- **[SYNC_USERS.md](SYNC_USERS.md)** - Sincronização de usuários PPPoE
- **[SYNC_PROFILES.md](SYNC_PROFILES.md)** - Sincronização de perfis
- **[SYNC_PROFILES_IMPLEMENTATION.md](SYNC_PROFILES_IMPLEMENTATION.md)** - Detalhes técnicos
- **[SYNC_FEATURE_SUMMARY.md](SYNC_FEATURE_SUMMARY.md)** - Resumo
- **[VALIDATION_CPF_CNPJ.md](VALIDATION_CPF_CNPJ.md)** - Validação de documentos

### 🧪 Testes
- **[TEST_DATA_CPF_CNPJ.md](TEST_DATA_CPF_CNPJ.md)** - CPFs e CNPJs válidos para teste

### 📊 Status
- **[IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md)** - Status de implementação
- **[CHECKLIST.md](CHECKLIST.md)** - Checklist de funcionalidades
- **[CONCLUSION.md](CONCLUSION.md)** - Conclusão e próximos passos

### 📢 Marketing
- **[LINKEDIN_POST.md](LINKEDIN_POST.md)** - Posts para LinkedIn (4 versões)

### 🗄️ Banco de Dados
- **[schema.sql](schema.sql)** - Schema completo (multi-tenant)

### 🧪 Scripts
- **[verify-implementation.sh](verify-implementation.sh)** - Verificação de implementação

---

## 🎯 Documentos Essenciais

### Top 5 para Começar:

1. 🥇 **[QUICK_START.md](QUICK_START.md)** - Setup em 5 minutos
2. 🥈 **[API_README.md](API_README.md)** - Documentação da API (70+ endpoints)
3. 🥉 **[ARCHITECTURE.md](ARCHITECTURE.md)** - Arquitetura do sistema
4. 🏅 **[DEVELOPMENT.md](DEVELOPMENT.md)** - Padrões de código
5. 🏅 **[ROADMAP.md](ROADMAP.md)** - Planejamento completo

---

## 🔍 Busca Rápida

| Preciso... | Veja... |
|------------|---------|
| Configurar ambiente | [QUICK_START.md](QUICK_START.md) |
| Entender a API | [API_README.md](API_README.md) |
| Ver arquitetura | [ARCHITECTURE.md](ARCHITECTURE.md) |
| Contribuir | [DEVELOPMENT.md](DEVELOPMENT.md) |
| Testar endpoints | [requests.http](requests.http) |
| Deploy | [START.md](START.md) |
| Banco de dados | [schema.sql](schema.sql) |
| Sincronização | [SYNC_USERS.md](SYNC_USERS.md) |
| Roadmap | [ROADMAP.md](ROADMAP.md) |
| Divulgar | [LINKEDIN_POST.md](LINKEDIN_POST.md) |

---

## 📊 Estatísticas

- **📄 Total de Documentos:** 22 arquivos
- **📝 Linhas de Documentação:** ~15.000+
- **🌍 Idiomas:** Português BR, English
- **✅ Status:** Completa e Organizada

---

## 🗺️ Índice Completo

Para uma visão completa e organizada de toda a documentação, consulte:

### **[📚 DOCS_INDEX.md](DOCS_INDEX.md)**
*Índice master com trilhas de aprendizado personalizadas por perfil*

---

## 💡 Dica Rápida

```bash
# Primeira vez?
1. Leia:  QUICK_START.md
2. Configure: docker-compose up -d
3. Execute: mvn spring-boot:run
4. Acesse: http://localhost:8080/swagger-ui.html
5. Teste:  requests.http (REST Client)
```

---

## 🤝 Contribuindo

Quer contribuir? Ótimo! Siga estes passos:

1. Leia [../README.md](../README.md#como-contribuir)
2. Leia [DEVELOPMENT.md](DEVELOPMENT.md)
3. Faça fork do repositório
4. Crie uma branch: `git checkout -b feature/minha-feature`
5. Commit: `git commit -m 'feat: adiciona minha feature'`
6. Push: `git push origin feature/minha-feature`
7. Abra um Pull Request

---

## 📞 Suporte e Comunidade

- 🐛 **Issues:** [GitHub Issues](https://github.com/seu-usuario/mikrotik/issues)
- 💬 **Discussões:** [GitHub Discussions](https://github.com/seu-usuario/mikrotik/discussions)
- 📖 **Wiki:** [GitHub Wiki](https://github.com/seu-usuario/mikrotik/wiki)
- 🌐 **API Docs:** `http://localhost:8080/swagger-ui.html`

---

## 🌟 Sobre o Projeto

**ISP Management API** é um sistema completo de gerenciamento para provedores de internet, integrando:

- 🏢 **Multi-tenant** - Múltiplas empresas
- 👥 **CRM** - Gestão de clientes
- 🔧 **Técnica** - Integração MikroTik via SSH
- 💼 **Comercial** - Contratos e planos
- 💰 **Financeiro** - Faturas e pagamentos
- 🤖 **Automação** - Jobs e auditoria

**Stack:** Java 21 • Spring Boot • MySQL • JWT • Docker

---

**📅 Última Atualização:** Janeiro 2026  
**👤 Mantido por:** Tiago Almeida  
**📄 Licença:** MIT  
**⭐ Contribuições:** Bem-vindas!
