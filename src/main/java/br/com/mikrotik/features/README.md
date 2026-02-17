# 🏗️ Arquitetura Feature-Oriented

Este projeto foi **reorganizado** para seguir uma arquitetura **feature-oriented** (orientada a features), mantendo **coerência com o front-end**.

## 📁 Estrutura de Diretórios

```
src/main/java/br/com/mikrotik/
├── features/                       ← Features auto-contidas
│   ├── auth/                       ← 🔐 Autenticação e Autorização
│   │   ├── controller/             → AuthController, ApiUserController
│   │   ├── service/                → ApiUserService, CustomUserDetailsService
│   │   ├── repository/             → ApiUserRepository
│   │   ├── model/                  → ApiUser, UserRole
│   │   └── dto/                    → LoginDTO, LoginResponseDTO, UserInfoDTO
│   │
│   ├── companies/                  ← 🏢 Multi-tenant (Empresas)
│   │   ├── controller/             → CompanyController
│   │   ├── service/                → CompanyService
│   │   ├── repository/             → CompanyRepository
│   │   ├── model/                  → Company
│   │   └── dto/                    → CompanyDTO
│   │
│   ├── customers/                  ← 👥 Clientes e Endereços
│   │   ├── controller/             → CustomerController, AddressController
│   │   ├── service/                → CustomerService, AddressService
│   │   ├── repository/             → CustomerRepository, AddressRepository
│   │   ├── model/                  → Customer, Address
│   │   └── dto/                    → CustomerDTO, AddressDTO
│   │
│   ├── contracts/                  ← 📄 Contratos e Planos
│   │   ├── controller/             → ContractController, ServicePlanController
│   │   ├── service/                → ContractService, ServicePlanService
│   │   ├── repository/             → ContractRepository, ServicePlanRepository
│   │   ├── model/                  → Contract, ServicePlan
│   │   └── dto/                    → ContractDTO, ServicePlanDTO
│   │
│   ├── invoices/                   ← 💰 Faturamento Recorrente
│   │   ├── controller/             → InvoiceController
│   │   ├── service/                → InvoiceService
│   │   ├── repository/             → InvoiceRepository
│   │   ├── model/                  → Invoice
│   │   ├── dto/                    → InvoiceDTO
│   │   ├── job/                    → InvoiceBillingJob (scheduled)
│   │   ├── event/                  → InvoicePaidEvent, InvoiceCreatedEvent
│   │   └── listener/               → InvoicePaidListener
│   │
│   ├── network/                    ← 🌐 Infraestrutura de Rede (Mikrotik/PPPoE)
│   │   ├── server/                 ← Servidores Mikrotik
│   │   │   ├── controller/         → MikrotikServerController, MikrotikDebugController
│   │   │   ├── service/            → MikrotikServerService
│   │   │   ├── adapter/            → MikrotikApiService, MikrotikSshService
│   │   │   ├── repository/         → MikrotikServerRepository
│   │   │   ├── model/              → MikrotikServer
│   │   │   └── dto/                → MikrotikServerDTO, ConnectionStatusDTO
│   │   │
│   │   ├── pppoe/                  ← PPPoE Users/Profiles/Connections
│   │   │   ├── controller/         → PppoeUserController, PppoeProfileController
│   │   │   ├── service/            → PppoeUserService, PppoeProfileService
│   │   │   ├── repository/         → PppoeUserRepository, PppoeProfileRepository
│   │   │   ├── model/              → PppoeUser, PppoeProfile, PppoeConnection
│   │   │   └── dto/                → PppoeUserDTO, PppoeProfileDTO
│   │   │
│   │   └── ippool/                 ← Pools de IPs
│   │       ├── controller/         → IpPoolController
│   │       ├── service/            → IpPoolService
│   │       ├── repository/         → IpPoolRepository
│   │       ├── model/              → IpPool
│   │       └── dto/                → IpPoolDTO
│   │
│   ├── financial/                  ← 💵 Fluxo de Caixa e Contabilidade
│   │   ├── controller/             → BankAccountController, TransactionController
│   │   ├── service/                → BankAccountService, CashFlowService
│   │   ├── repository/             → BankAccountRepository, TransactionRepository
│   │   ├── model/                  → BankAccount, Transaction, ChartOfAccounts
│   │   └── dto/                    → BankAccountDTO, TransactionDTO
│   │
│   ├── dashboard/                  ← 📊 Estatísticas e Indicadores
│   │   ├── controller/             → DashboardController
│   │   ├── service/                → DashboardService
│   │   └── dto/                    → DashboardStatsDTO
│   │
│   ├── sync/                       ← 🔄 Sincronização Mikrotik → Sistema
│   │   ├── controller/             → FullSyncController
│   │   ├── service/                → FullSyncService
│   │   └── dto/                    → FullSyncConfigDTO, FullSyncResultDTO
│   │
│   └── automation/                 ← 🤖 Logs de Automação
│       ├── controller/             → AutomationLogController
│       ├── service/                → AutomationLogService
│       ├── repository/             → AutomationLogRepository
│       ├── model/                  → AutomationLog
│       └── dto/                    → AutomationLogDTO
│
├── shared/                         ← 🔧 Cross-cutting concerns
│   ├── config/                     → Configurações Spring, Beans, CORS
│   ├── security/                   → JwtTokenProvider, JwtAuthenticationFilter
│   ├── exception/                  → GlobalExceptionHandler
│   ├── util/                       → DocumentValidator, CompanyContextHolder
│   └── constant/                   → Enums e constantes globais
│
└── MikrotikApplication.java        ← Main class
```

---

## 🎯 Princípios da Arquitetura

### 1. Feature Auto-Contida (Self-Contained)

Cada feature contém **todos** os seus artefatos relacionados:

- ✅ **Controllers** (API REST)
- ✅ **Services** (Lógica de negócio)
- ✅ **Repositories** (Acesso a dados)
- ✅ **Models** (Entidades JPA)
- ✅ **DTOs** (Data Transfer Objects)
- ✅ **Events/Listeners** (Eventos de domínio)

**Benefício:** Um desenvolvedor encontra **tudo** relacionado a "Contratos" em `features/contracts/`.

---

### 2. Coerência Front-Back

```
Backend: features/customers/     →  Frontend: app/customers/
Backend: features/invoices/      →  Frontend: app/invoices/
Backend: features/network/       →  Frontend: app/network/
```

Um desenvolvedor **full-stack** navega mentalmente entre front e back usando a **mesma estrutura**.

---

### 3. Baixo Acoplamento

- Features se comunicam via **Eventos** ou **DTOs públicos**
- Repositories e Services são **package-private** quando possível
- Apenas Controllers e DTOs são públicos

**Exemplo:**

```java
// ❌ Evitar import direto de Service de outra feature
import br.com.mikrotik.features.invoices.service.InvoiceService;

// ✅ Usar Events ou DTOs públicos
import br.com.mikrotik.features.invoices.dto.InvoiceDTO;
import br.com.mikrotik.features.invoices.event.InvoicePaidEvent;
```

---

### 4. Bounded Context (DDD)

Cada feature representa um **bounded context**:

- `invoices/` = Contexto de **Faturamento**
- `network/` = Contexto de **Infraestrutura**
- `customers/` = Contexto de **CRM**

---

## 🔀 Comunicação Entre Features

### Eventos de Domínio (Recomendado)

```java
// Em features/invoices/event/InvoicePaidEvent.java
public class InvoicePaidEvent {
    private Long invoiceId;
    private Long contractId;
    private BigDecimal amount;
}

// Em features/network/listener/
@Component
public class NetworkListener {
    @EventListener
    public void onInvoicePaid(InvoicePaidEvent event) {
        // Liberar PPPoE User após pagamento
    }
}
```

### DTOs Públicos (Quando necessário)

```java
// Em features/contracts/service/ContractService.java
import br.com.mikrotik.features.network.pppoe.dto.PppoeUserDTO;
```

---

## 🚀 Vantagens

### Para Desenvolvedores Juniores

✅ **Fácil localização**: "Onde fica o código de contratos?" → `features/contracts/`  
✅ **Contexto claro**: Tudo relacionado a contratos está junto  
✅ **Menos sobrecarga cognitiva**: Não precisa navegar entre 5 pastas diferentes

### Para Desenvolvedores Sêniores

✅ **Isolamento de domínio**: Mudanças em `invoices` não afetam `network`  
✅ **Testes focados**: Testar uma feature por vez  
✅ **Refatoração segura**: Bounded contexts claros

### Para a Equipe

✅ **Coerência com Front-end**: Mesma estrutura mental  
✅ **Onboarding rápido**: Estrutura auto-explicativa  
✅ **Code Review facilitado**: "Este PR afeta apenas `features/customers/`"

---

## 📚 Convenções

### Nomenclatura de Pacotes

```java
package br.com.mikrotik.features.customers.controller;
package br.com.mikrotik.features.customers.service;
package br.com.mikrotik.features.customers.repository;
```

### Visibilidade

```java
// Controllers - Public (API REST)
@RestController
public class CustomerController { }

// Services - Package-private quando possível
@Service
class CustomerService { }

// Repositories - Package-private
interface CustomerRepository extends JpaRepository { }
```

---

## 📖 Regras de Negócio Críticas

### Invoices (Faturamento)

⚠️ **NUNCA** executar integrações HTTP/SSH dentro de `@Transactional`  
✅ Persista a intenção no banco → Feche a transação → Execute integração assíncrona

### Network (Mikrotik)

⚠️ **Retry Pattern** obrigatório para falhas de rede  
⚠️ **Circuit Breaker** para servidores offline  
✅ Integrações são **sempre** assíncronas (fora de transações)

### Financial (Fluxo de Caixa)

⚠️ **Consistência forte (ACID)** obrigatória  
⚠️ Transações financeiras nunca podem ser perdidas  
✅ Dead Letter Queue para falhas

---

## 🔧 Migração Concluída

✅ **118 arquivos** reorganizados e imports atualizados  
✅ Estrutura coerente com front-end  
✅ Pastas antigas removidas  
✅ Pronto para desenvolvimento

---

## 🛠️ Próximos Passos

1. **Compilar projeto:**
   ```bash
   ./mvnw clean compile
   ```

2. **Rodar testes:**
   ```bash
   ./mvnw test
   ```

3. **Verificar erros:**
   ```bash
   ./mvnw clean install
   ```

---

**Data da Migração**: 2026-02-16  
**Padrão**: Feature-Oriented Architecture  
**Inspiração**: Angular/React feature modules  
**Total de arquivos migrados**: 118

---

## 📞 Dúvidas?

Leia os READMEs específicos de cada feature:
- `features/auth/README.md`
- `features/invoices/README.md`
- `features/network/README.md`
- etc.

