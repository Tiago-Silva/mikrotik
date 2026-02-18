# 🔧 Recursos Compartilhados

Documentação e recursos utilizados por múltiplas features do sistema.

---

## 📋 Conteúdo

### 🧪 Dados de Teste
- **[TEST_DATA_CPF_CNPJ.md](TEST_DATA_CPF_CNPJ.md)** - CPFs e CNPJs válidos para testes

---

## 🎯 Como Usar

### CPFs/CNPJs para Testes

Ao testar funcionalidades de cadastro de clientes (CRM), contratos ou faturas, use os documentos válidos fornecidos em `TEST_DATA_CPF_CNPJ.md`.

**Exemplos:**
- CPF: `111.444.777-35` ou `11144477735`
- CNPJ: `11.222.333/0001-81` ou `11222333000181`

### Validação Automática

O sistema valida automaticamente CPFs e CNPJs usando o algoritmo de dígitos verificadores. Documentos inválidos serão rejeitados pela API.

---

## 🔗 Features que Usam Estes Recursos

- **[customers/](../customers/)** - Cadastro de clientes PF/PJ
- **[contracts/](../contracts/)** - Criação de contratos vinculados a clientes
- **[invoices/](../invoices/)** - Geração de faturas para clientes
- **[financial/](../financial/)** - Transações e recebimentos

---

## 📝 Convenções

### Para Adicionar Novos Recursos Compartilhados

Se você identificar documentação ou utilitários que são usados por **2 ou mais features**, considere adicioná-los aqui:

1. Crie o arquivo markdown com nome descritivo
2. Adicione link neste README.md
3. Referencie o arquivo nos READMEs das features que o usam

### Exemplos de Conteúdo Futuro
- Códigos de erro HTTP padronizados
- Formatos de data/hora aceitos
- Limites e quotas do sistema
- Glossário de termos de negócio

---

**Última atualização:** Fevereiro 2026

