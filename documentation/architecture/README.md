# 🏛️ Arquitetura do Sistema

Documentação completa sobre a arquitetura, decisões técnicas e evolução estrutural do sistema ISP Management.

---

## 📚 Documentos Disponíveis

### 🏗️ Arquitetura Principal
- **[ARCHITECTURE_ACTUAL.md](ARCHITECTURE_ACTUAL.md)** - ⭐ **Arquitetura atual em produção** (Package-by-Feature + DDD)
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Arquitetura em camadas (versão legada para referência)

### 🔄 Refatoração e Evolução
- **[REFACTORING_GUIDE.md](REFACTORING_GUIDE.md)** - 🛠️ **Guia completo de refatoração** para Modular Monolith + Hexagonal
- **[ROADMAP.md](ROADMAP.md)** - 🗺️ **Roadmap de evolução** (9 fases planejadas)

---

## 🎯 Como Usar Esta Documentação

### 👨‍💻 Para Desenvolvedores
**Leia nesta ordem:**
1. `ARCHITECTURE_ACTUAL.md` - Entenda a estrutura atual
2. `REFACTORING_GUIDE.md` - Veja como contribuir seguindo os padrões
3. `ROADMAP.md` - Conheça as próximas fases

### 🏢 Para Arquitetos/Tech Leads
**Documentos estratégicos:**
- `ARCHITECTURE_ACTUAL.md` - Decisões de design e bounded contexts
- `REFACTORING_GUIDE.md` - Padrões e proteções transacionais
- `ROADMAP.md` - Planejamento de escalabilidade

### 📖 Para Estudantes/Pesquisadores
**Evolução do projeto:**
- `ARCHITECTURE.md` (legado) → `ARCHITECTURE_ACTUAL.md` (atual)
- Veja como o sistema evoluiu de camadas clássicas para DDD modular

---

## 🧠 Conceitos-Chave

### Bounded Contexts Implementados
```
📦 Sistema ISP Management
├── 🔐 Auth (IAM)           - Identity & Access Management
├── 👥 CRM                  - Customer Relationship Management
├── 📝 Contracts            - Contratos e Planos de Serviço
├── 💰 Billing (Financial)  - Faturamento e Transações (ACID)
├── 🌐 Network              - Integração Mikrotik (Eventual Consistency)
├── 🤖 Automation           - Jobs e Processamento Assíncrono
└── 📊 Dashboard            - Métricas e Relatórios
```

### Padrões Arquiteturais
- **Modular Monolith** - Preparado para microservices
- **Hexagonal Architecture** - Ports & Adapters
- **Domain-Driven Design** - Bounded Contexts e Aggregates
- **CQRS (parcial)** - Separação de comandos e consultas críticas

### Princípios de Proteção
- ⚠️ **NUNCA** fazer chamadas externas dentro de `@Transactional`
- 💰 **Billing** - Consistência forte (ACID)
- 🌐 **Network** - Consistência eventual + retries
- 🔄 **Event-Driven** - Para integrações assíncronas

---

## 🔗 Navegação

- **Voltar para índice master:** [`/documentation/DOCS_INDEX.md`](../DOCS_INDEX.md)
- **Ver features específicas:** [`/documentation/`](../)
  - [`auth/`](../auth/) - Autenticação e usuários
  - [`financial/`](../financial/) - Fluxo de caixa
  - [`invoices/`](../invoices/) - Faturamento
  - [`network/`](../network/) - Integração Mikrotik
  - [`sync/`](../sync/) - Sincronização

---

**Última atualização:** Fevereiro 2026  
**Mantenedor:** Tiago (Backend Principal Engineer)

