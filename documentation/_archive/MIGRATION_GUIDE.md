# 📦 Guia de Migração da Documentação

**Data:** 2026-02-18  
**Versão:** 1.0  
**Status:** ✅ Implementado

---

## 🎯 O Que Mudou?

A documentação foi **reorganizada por features** para espelhar a arquitetura **Package-by-Feature** do código-fonte, facilitando a descoberta e manutenção de documentos.

### ❌ Estrutura ANTES (Plana)
```
documentation/
├── USER_MANAGEMENT_API.md
├── CASH_FLOW_API_GUIDE.md
├── BANK_ACCOUNT_TYPES.md
├── ASYNC_INTEGRATION_GUIDE.md
├── SYNC_USERS.md
├── SYNC_PROFILES.md
├── (30+ arquivos misturados)
└── invoices/
    └── (4 arquivos)
```

### ✅ Estrutura AGORA (Por Feature)
```
documentation/
├── README.md (principal)
├── DOCS_INDEX.md (índice master)
├── ARCHITECTURE_ACTUAL.md
├── auth/
│   ├── README.md
│   └── USER_MANAGEMENT_API.md
├── customers/
│   └── README.md
├── contracts/
│   └── README.md
├── invoices/
│   ├── README.md
│   ├── AUTOMATIC_REACTIVATION_FLOW.md
│   └── ...
├── financial/
│   ├── README.md
│   ├── CASH_FLOW_API_GUIDE.md
│   └── BANK_ACCOUNT_TYPES.md
├── network/
│   ├── README.md
│   └── ASYNC_INTEGRATION_GUIDE.md
└── sync/
    ├── README.md
    ├── SYNC_USERS.md
    ├── SYNC_PROFILES.md
    └── ...
```

---

## 📋 Mapa de Migração de Arquivos

### Arquivos Movidos

| Localização ANTIGA | Localização NOVA | Feature |
|-------------------|------------------|---------|
| `USER_MANAGEMENT_API.md` | `auth/USER_MANAGEMENT_API.md` | 🔐 Autenticação |
| `CASH_FLOW_API_GUIDE.md` | `financial/CASH_FLOW_API_GUIDE.md` | 💰 Financeiro |
| `BANK_ACCOUNT_TYPES.md` | `financial/BANK_ACCOUNT_TYPES.md` | 💰 Financeiro |
| `ASYNC_INTEGRATION_GUIDE.md` | `network/ASYNC_INTEGRATION_GUIDE.md` | 🌐 Rede/Mikrotik |
| `SYNC_USERS.md` | `sync/SYNC_USERS.md` | 🔄 Sincronização |
| `SYNC_PROFILES.md` | `sync/SYNC_PROFILES.md` | 🔄 Sincronização |
| `SYNC_PROFILES_IMPLEMENTATION.md` | `sync/SYNC_PROFILES_IMPLEMENTATION.md` | 🔄 Sincronização |
| `SYNC_FEATURE_SUMMARY.md` | `sync/SYNC_FEATURE_SUMMARY.md` | 🔄 Sincronização |

### Arquivos Criados (Novos)

| Arquivo | Descrição |
|---------|-----------|
| `auth/README.md` | Índice da feature de autenticação |
| `customers/README.md` | Índice da feature de clientes |
| `contracts/README.md` | Índice da feature de contratos |
| `invoices/README.md` | Índice da feature de faturamento |
| `financial/README.md` | Índice da feature financeira |
| `network/README.md` | Índice da feature de rede |
| `sync/README.md` | Índice da feature de sincronização |

### Arquivos Não Movidos (Raiz)

Documentos arquiteturais e globais permanecem na raiz:

| Arquivo | Por quê? |
|---------|----------|
| `README.md` | Porta de entrada principal |
| `DOCS_INDEX.md` | Índice master atualizado |
| `ARCHITECTURE_ACTUAL.md` | Arquitetura global do sistema |
| `ARCHITECTURE.md` | Arquitetura legado (referência) |
| `REFACTORING_GUIDE.md` | Guia arquitetural geral |
| `ROADMAP.md` | Planejamento global |
| `QUICK_START.md` | Setup inicial |
| `START.md` | Instalação detalhada |
| `DEVELOPMENT.md` | Padrões de código |
| `API_README.md` | Documentação geral da API |
| `requests.http` | Exemplos HTTP gerais |
| `schema.sql` | Schema do banco |
| `TEST_DATA_CPF_CNPJ.md` | Dados de teste globais |

---

## 🔗 Atualizar Links nos Seus Bookmarks

Se você tinha bookmarks/favoritos apontando para documentos antigos, atualize:

### Exemplos de Atualização

```
❌ ANTIGO: documentation/USER_MANAGEMENT_API.md
✅ NOVO:   documentation/auth/USER_MANAGEMENT_API.md

❌ ANTIGO: documentation/ASYNC_INTEGRATION_GUIDE.md
✅ NOVO:   documentation/network/ASYNC_INTEGRATION_GUIDE.md

❌ ANTIGO: documentation/SYNC_USERS.md
✅ NOVO:   documentation/sync/SYNC_USERS.md

❌ ANTIGO: documentation/CASH_FLOW_API_GUIDE.md
✅ NOVO:   documentation/financial/CASH_FLOW_API_GUIDE.md
```

---

## 🛠️ Script de Atualização de Links (Opcional)

Se você tem documentos externos ou READMEs linkando para os documentos antigos, use este script:

```bash
#!/bin/bash
# update-doc-links.sh

cd /seu/projeto/documentation

# Atualizar links em todos os arquivos markdown
find . -name "*.md" -type f -exec sed -i \
  -e 's|documentation/USER_MANAGEMENT_API.md|documentation/auth/USER_MANAGEMENT_API.md|g' \
  -e 's|documentation/ASYNC_INTEGRATION_GUIDE.md|documentation/network/ASYNC_INTEGRATION_GUIDE.md|g' \
  -e 's|documentation/CASH_FLOW_API_GUIDE.md|documentation/financial/CASH_FLOW_API_GUIDE.md|g' \
  -e 's|documentation/BANK_ACCOUNT_TYPES.md|documentation/financial/BANK_ACCOUNT_TYPES.md|g' \
  -e 's|documentation/SYNC_USERS.md|documentation/sync/SYNC_USERS.md|g' \
  -e 's|documentation/SYNC_PROFILES.md|documentation/sync/SYNC_PROFILES.md|g' \
  -e 's|documentation/SYNC_PROFILES_IMPLEMENTATION.md|documentation/sync/SYNC_PROFILES_IMPLEMENTATION.md|g' \
  -e 's|documentation/SYNC_FEATURE_SUMMARY.md|documentation/sync/SYNC_FEATURE_SUMMARY.md|g' \
  {} +

echo "✅ Links atualizados!"
```

**Uso:**
```bash
chmod +x update-doc-links.sh
./update-doc-links.sh
```

---

## 📚 Novos Pontos de Entrada por Feature

Agora cada feature tem um `README.md` central que serve como **índice local**:

### Como Usar

1. **Quer entender autenticação?**
   - Comece em: `documentation/auth/README.md`
   - Veja guia completo: `documentation/auth/USER_MANAGEMENT_API.md`

2. **Quer entender integração Mikrotik?**
   - Comece em: `documentation/network/README.md`
   - Veja guia detalhado: `documentation/network/ASYNC_INTEGRATION_GUIDE.md`

3. **Quer entender sincronização?**
   - Comece em: `documentation/sync/README.md`
   - Escolha: `SYNC_USERS.md` ou `SYNC_PROFILES.md`

4. **Quer entender faturamento?**
   - Comece em: `documentation/invoices/README.md`
   - Veja fluxos: `AUTOMATIC_REACTIVATION_FLOW.md`, etc.

---

## ✅ Checklist de Migração (Para Desenvolvedores)

### Se você é desenvolvedor no projeto:

- [ ] Atualizar bookmarks/favoritos no navegador
- [ ] Atualizar links em documentos externos (Confluence, Notion, etc.)
- [ ] Atualizar links em README.md de outros repositórios
- [ ] Notificar time sobre nova estrutura
- [ ] Revisar `DOCS_INDEX.md` para trilhas de onboarding atualizadas

### Se você é novo no projeto:

- [ ] Ler `documentation/README.md` (porta de entrada)
- [ ] Ler `documentation/DOCS_INDEX.md` (índice master)
- [ ] Explorar features via `<feature>/README.md`
- [ ] Seguir trilha de onboarding em `DOCS_INDEX.md`

---

## 🎯 Benefícios da Nova Estrutura

### ✅ Para Desenvolvedores
- 🔍 **Fácil localização**: Documentos agrupados por contexto de negócio
- 🧩 **Coesão**: Documentação espelha estrutura do código
- 🚀 **Onboarding rápido**: READMEs de feature como guias iniciais
- 🔗 **Referências cruzadas**: Links claros entre features relacionadas

### ✅ Para o Projeto
- 📦 **Modularidade**: Preparação para futura separação em microserviços
- 🛠️ **Manutenibilidade**: Documentação organizada facilita updates
- 📊 **Escalabilidade**: Adicionar nova feature = criar nova pasta
- 🎓 **Documentação viva**: READMEs de feature mantidos próximos ao código

---

## 🚨 Troubleshooting

### Problema: "Link quebrado para documento antigo"

**Causa:** Algum documento ainda referencia localização antiga

**Solução:**
1. Verificar qual documento tem o link quebrado
2. Atualizar para nova localização usando tabela de migração acima
3. Ou usar script de atualização de links

### Problema: "Não encontro documento X"

**Solução:**
1. Verificar `DOCS_INDEX.md` (índice master atualizado)
2. Verificar tabela de migração neste documento
3. Usar busca do GitHub: `filename:NOME_DO_ARQUIVO.md`

### Problema: "README.md da feature está vazio"

**Causa:** Feature é placeholder para desenvolvimento futuro

**Solução:**
- Features `customers/` e `contracts/` têm READMEs básicos
- Documentação específica será adicionada conforme necessário
- Contribua criando pull request com documentação!

---

## 📞 Suporte

Dúvidas sobre a migração?
- 📧 Abra issue no GitHub
- 💬 Pergunte no canal #documentation do Slack
- 👥 Consulte `DOCS_INDEX.md` para trilhas por perfil

---

**📅 Data da migração:** 2026-02-18  
**🔄 Impacto:** Baixo (apenas organização)  
**⚠️ Breaking change:** Não (arquivos movidos, não deletados)  
**✅ Status:** Completo e validado

