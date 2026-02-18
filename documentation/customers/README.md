
# 👥 Clientes

## 📋 Visão Geral

Módulo responsável por cadastro e gestão de clientes (pessoas físicas e jurídicas), incluindo informações de contato, endereço e documentos.

---

## 📚 Documentação Disponível

🚧 **Em construção** - Documentação específica será adicionada em breve.

---

## 🎯 Funcionalidades Principais

### ✅ Implementado
- **CRUD de Clientes** - Criar, listar, atualizar, desativar
- **Pessoa Física e Jurídica** - CPF e CNPJ
- **Validação de Documentos** - CPF/CNPJ válidos
- **Endereço Completo** - CEP, rua, número, bairro, cidade, estado
- **Contatos** - Telefone, celular, e-mail
- **Multi-tenant** - Isolamento por empresa
- **Soft Delete** - Desativa ao invés de excluir
- **Busca Avançada** - Por nome, documento, e-mail

### 🚧 Roadmap
- [ ] Integração com API ViaCEP (busca automática de endereço)
- [ ] Upload de documentos (RG, CNH, Comprovante de Residência)
- [ ] Histórico de endereços (mudanças)
- [ ] Múltiplos contatos por cliente
- [ ] Tags e segmentação de clientes
- [ ] Score de crédito

---

## 🔗 Referências Relacionadas

**Arquitetura:**
- [../ARCHITECTURE_ACTUAL.md](../ARCHITECTURE_ACTUAL.md) - Arquitetura do sistema
- [../TEST_DATA_CPF_CNPJ.md](../TEST_DATA_CPF_CNPJ.md) - CPFs/CNPJs válidos para testes

**Outras Features:**
- [../contracts/](../contracts/) - Contratos vinculados ao cliente
- [../invoices/](../invoices/) - Faturas do cliente
- [../auth/](../auth/) - Permissões de acesso

**Código:**
- `CustomerService.java` - Lógica de negócio
- `CustomerRepository.java` - Persistência
- `CPFCNPJValidator.java` - Validação de documentos

---

## 🛠️ Endpoints Principais

```
GET    /api/customers               - Listar clientes (paginado)
POST   /api/customers               - Criar cliente
GET    /api/customers/{id}          - Buscar cliente por ID
PUT    /api/customers/{id}          - Atualizar cliente
DELETE /api/customers/{id}          - Desativar cliente (soft delete)

GET    /api/customers/search?q=     - Buscar por nome/documento
GET    /api/customers/{id}/contracts - Contratos do cliente
GET    /api/customers/{id}/invoices  - Faturas do cliente
```

---

## 📊 Tipos de Cliente

| Tipo | Documento | Campos Adicionais |
|------|-----------|-------------------|
| **Pessoa Física** | CPF (11 dígitos) | Nome completo, Data de nascimento |
| **Pessoa Jurídica** | CNPJ (14 dígitos) | Razão social, Nome fantasia, IE |

---

## 💡 Regras de Negócio

### Cadastro
- ✅ CPF/CNPJ deve ser válido (validação de dígitos verificadores)
- ✅ CPF/CNPJ deve ser único por empresa
- ✅ E-mail deve ser válido
- ✅ Telefone deve ter formato válido (DDD + número)

### Atualização
- ✅ Não pode alterar CPF/CNPJ após criação
- ✅ Alteração de e-mail requer confirmação (futuro)
- ✅ Histórico de alterações é mantido (auditoria)

### Exclusão
- ✅ Soft delete (não remove do banco)
- ✅ Cliente com contratos ativos não pode ser desativado
- ✅ Faturas pendentes impedem desativação

---

## 🧪 Testes

```bash
# Criar cliente PF
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "cpfCnpj": "12345678901",
    "email": "joao@exemplo.com",
    "phone": "11987654321",
    "address": {
      "zipCode": "01310-100",
      "street": "Av. Paulista",
      "number": "1000",
      "city": "São Paulo",
      "state": "SP"
    }
  }'

# Buscar cliente
curl http://localhost:8080/api/customers/1 \
  -H "Authorization: Bearer TOKEN"

# Listar contratos do cliente
curl http://localhost:8080/api/customers/1/contracts \
  -H "Authorization: Bearer TOKEN"
```

Veja CPFs/CNPJs válidos para testes: [../TEST_DATA_CPF_CNPJ.md](../TEST_DATA_CPF_CNPJ.md)

---

## 🔐 Segurança

### Validações
- ✅ CPF/CNPJ com validação de dígitos verificadores
- ✅ E-mail com formato RFC válido
- ✅ Telefone com formato brasileiro (DDD + número)

### LGPD (Lei Geral de Proteção de Dados)
- ⚠️ Dados sensíveis devem ser criptografados em repouso
- ⚠️ Cliente pode solicitar exclusão de dados (direito ao esquecimento)
- ⚠️ Logs de acesso a dados de clientes (auditoria)

### Multi-tenant
- ✅ Isolamento por empresa
- ✅ Usuário só vê clientes da sua empresa
- ✅ Validação de propriedade em todas as operações

---

## 📈 Métricas

```sql
-- Total de clientes por tipo
SELECT 
    CASE WHEN LENGTH(cpf_cnpj) = 11 THEN 'PF' ELSE 'PJ' END as tipo,
    COUNT(*) as total
FROM customers
WHERE active = true
GROUP BY tipo;

-- Clientes sem contrato ativo
SELECT c.* 
FROM customers c
LEFT JOIN contracts ct ON ct.customer_id = c.id AND ct.status = 'ACTIVE'
WHERE ct.id IS NULL AND c.active = true;
```

---

## 🚨 Troubleshooting

### Problema: "CPF/CNPJ inválido"
**Causa:** Documento não passa na validação de dígitos verificadores

**Solução:**
- Verificar se digitou corretamente
- Usar CPFs/CNPJs de teste válidos: [../TEST_DATA_CPF_CNPJ.md](../TEST_DATA_CPF_CNPJ.md)
- Testar validação online: [Gerador de CPF](https://www.4devs.com.br/gerador_de_cpf)

### Problema: "E-mail já cadastrado"
**Causa:** E-mail duplicado na mesma empresa

**Solução:**
- Verificar se cliente já existe: `GET /api/customers/search?q=email@exemplo.com`
- Usar e-mail diferente ou reativar cliente existente

---

**📅 Última atualização:** 2026-02-18  
**👤 Responsável:** Backend Team  
**🔄 Status:** ✅ Produção

