# 🧪 Dados de Teste - CPF e CNPJ Válidos

## 📋 CPFs Válidos para Teste

Use estes CPFs válidos em ambiente de desenvolvimento/teste:

### CPFs Formatados
```
111.444.777-35
123.456.789-09
987.654.321-00
529.982.247-25
123.789.456-07
666.777.888-95
```

### CPFs Sem Formatação
```
11144477735
12345678909
98765432100
52998224725
12378945607
66677788895
```

## 🏢 CNPJs Válidos para Teste

### CNPJs Formatados
```
11.222.333/0001-81
12.345.678/0001-95
98.765.432/0001-10
11.444.777/0001-61
```

### CNPJs Sem Formatação
```
11222333000181
12345678000195
98765432000110
11444777000161
```

## ⚠️ CPFs/CNPJs Inválidos (Para Testes Negativos)

### CPFs Inválidos
```
000.000.000-00  (todos zeros)
111.111.111-11  (dígitos repetidos)
123.456.789-00  (dígitos verificadores errados)
999.999.999-99  (dígitos repetidos)
12345678901     (menos de 11 dígitos)
```

### CNPJs Inválidos
```
00.000.000/0000-00  (todos zeros)
11.111.111/1111-11  (dígitos repetidos)
12.345.678/0001-00  (dígitos verificadores errados)
```

## 🔍 Como a Validação Funciona

### Validação de CPF

O sistema valida CPF seguindo o algoritmo oficial:

1. **Remove formatação** - Remove pontos e traços
2. **Verifica tamanho** - Deve ter exatamente 11 dígitos
3. **Verifica repetição** - Não aceita 111.111.111-11
4. **Calcula dígitos verificadores** - Valida os 2 últimos dígitos

**Código:**
```java
DocumentValidator.isValidCPF("12345678909")  // true
DocumentValidator.isValidCPF("123.456.789-09")  // true (aceita formatado)
DocumentValidator.isValidCPF("11111111111")  // false (repetido)
DocumentValidator.isValidCPF("12345678900")  // false (dígito errado)
```

### Validação de CNPJ

Segue o mesmo princípio do CPF:

1. **Remove formatação**
2. **Verifica tamanho** - Deve ter 14 dígitos
3. **Verifica repetição**
4. **Calcula dígitos verificadores**

**Código:**
```java
DocumentValidator.isValidCNPJ("11222333000181")  // true
DocumentValidator.isValidCNPJ("11.222.333/0001-81")  // true (formatado)
```

## 📝 Exemplos de Uso na API

### Criar Cliente Pessoa Física
```http
POST /api/customers
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "João Silva",
  "type": "FISICA",
  "document": "111.444.777-35",
  "email": "joao@example.com",
  "phonePrimary": "(11) 98765-4321"
}
```

### Criar Cliente Pessoa Jurídica
```http
POST /api/customers
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Empresa Tech LTDA",
  "type": "JURIDICA",
  "document": "11.222.333/0001-81",
  "email": "contato@empresa.com",
  "phonePrimary": "(11) 3333-4444"
}
```

### Teste de Validação (Esperado: Erro)
```http
POST /api/customers
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Cliente Teste",
  "type": "FISICA",
  "document": "111.111.111-11",
  "email": "teste@example.com"
}
```

**Resposta esperada:**
```json
{
  "timestamp": "2026-01-25T16:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "CPF inválido: 11111111111",
  "path": "/api/customers"
}
```

## 🛠️ Utilitários

### Formatação
```java
// Formatar CPF
String formatted = DocumentValidator.formatCPF("12345678909");
// Resultado: "123.456.789-09"

// Formatar CNPJ
String formatted = DocumentValidator.formatCNPJ("11222333000181");
// Resultado: "11.222.333/0001-81"

// Remover formatação
String clean = DocumentValidator.unformat("123.456.789-09");
// Resultado: "12345678909"
```

### Validação por Tipo
```java
// Validar baseado no tipo de cliente
boolean valid = DocumentValidator.isValidDocument("12345678909", "FISICA");
// true

boolean valid = DocumentValidator.isValidDocument("11222333000181", "JURIDICA");
// true
```

## 🧪 Casos de Teste

### Testes Positivos (Devem Passar)

| CPF/CNPJ | Tipo | Formatado | Não Formatado | Esperado |
|----------|------|-----------|---------------|----------|
| CPF | FISICA | ✅ | ✅ | Válido |
| CNPJ | JURIDICA | ✅ | ✅ | Válido |

### Testes Negativos (Devem Falhar)

| CPF/CNPJ | Motivo | Esperado |
|----------|--------|----------|
| 000.000.000-00 | Todos zeros | Inválido |
| 111.111.111-11 | Repetidos | Inválido |
| 123.456.789-00 | Dígito errado | Inválido |
| 1234567890 | Tamanho errado | Inválido |
| null | Nulo | Inválido |

## 📚 Referências

- **Classe:** `br.com.mikrotik.util.DocumentValidator`
- **Service:** `br.com.mikrotik.service.CustomerService.validateDocument()`
- **Documentação:** [VALIDATION_CPF_CNPJ.md](VALIDATION_CPF_CNPJ.md)

## 💡 Dicas

1. **Aceita formatado ou não** - O sistema remove a formatação automaticamente
2. **Armazena sem formatação** - No banco é salvo apenas números
3. **Validação automática** - Ao criar/atualizar cliente, valida automaticamente
4. **Mensagens claras** - Erros indicam exatamente o problema

## ⚡ Gerador Online de CPF/CNPJ

Para gerar mais CPFs/CNPJs válidos para teste, use:
- **CPF:** https://www.4devs.com.br/gerador_de_cpf
- **CNPJ:** https://www.4devs.com.br/gerador_de_cnpj

**⚠️ Atenção:** Use apenas em ambiente de desenvolvimento/teste!

---

**📅 Última Atualização:** Janeiro 2026  
**🔒 Segurança:** Dados fictícios para teste  
**✅ Status:** Validação implementada e funcional
