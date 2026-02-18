# 🛠️ Guia de Desenvolvimento - Mikrotik PPPoE Management API

## 📋 Pré-requisitos de Desenvolvimento

- **JDK 21+** - [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Git** - [Download](https://git-scm.com/downloads)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)
- **IDE recomendada:** IntelliJ IDEA Ultimate ou VS Code com extensões Java

## 🚀 Setup de Desenvolvimento

### 1. Clonar Repositório
```bash
git clone <repository-url>
cd mikrotik
```

### 2. Instalar Dependências
```bash
mvn clean install -DskipTests
```

### 3. Iniciar Banco de Dados
```bash
docker-compose up -d
docker-compose logs -f mysql
```

### 4. Executar Aplicação
```bash
mvn spring-boot:run
```

Ou via IDE - clique em "Run" no arquivo `MikrotikApplication.java`

### 5. Acessar Documentação
```
Swagger UI: http://localhost:8080/swagger-ui.html
API Docs: http://localhost:8080/v3/api-docs
```

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/br/com/mikrotik/
│   │   ├── MikrotikApplication.java          # Entrada da aplicação
│   │   ├── controller/                       # REST Controllers (camada web)
│   │   │   ├── AuthController.java
│   │   │   ├── MikrotikServerController.java
│   │   │   ├── PppoeProfileController.java
│   │   │   ├── PppoeUserController.java
│   │   │   └── PppoeConnectionController.java
│   │   ├── service/                          # Lógica de negócio
│   │   │   ├── MikrotikServerService.java
│   │   │   ├── PppoeProfileService.java
│   │   │   ├── PppoeUserService.java
│   │   │   ├── PppoeConnectionService.java
│   │   │   ├── MikrotikSshService.java       # Integração SSH
│   │   │   └── CustomUserDetailsService.java # Autenticação
│   │   ├── repository/                       # Acesso a dados (Spring Data JPA)
│   │   │   ├── ApiUserRepository.java
│   │   │   ├── MikrotikServerRepository.java
│   │   │   ├── PppoeProfileRepository.java
│   │   │   ├── PppoeUserRepository.java
│   │   │   ├── PppoeConnectionRepository.java
│   │   │   └── AuditLogRepository.java
│   │   ├── model/                            # Entidades JPA
│   │   │   ├── ApiUser.java
│   │   │   ├── MikrotikServer.java
│   │   │   ├── PppoeProfile.java
│   │   │   ├── PppoeUser.java
│   │   │   ├── PppoeConnection.java
│   │   │   └── AuditLog.java
│   │   ├── dto/                              # Data Transfer Objects
│   │   │   ├── LoginDTO.java
│   │   │   ├── LoginResponseDTO.java
│   │   │   ├── MikrotikServerDTO.java
│   │   │   ├── PppoeProfileDTO.java
│   │   │   ├── PppoeUserDTO.java
│   │   │   └── PppoeConnectionDTO.java
│   │   ├── exception/                        # Exceções customizadas
│   │   │   ├── ResourceNotFoundException.java
│   │   │   ├── MikrotikConnectionException.java
│   │   │   ├── ApiError.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── security/                         # JWT e Segurança
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── config/                           # Configurações Spring
│   │   │   ├── SecurityConfig.java
│   │   │   ├── OpenApiConfig.java
│   │   │   └── DataInitializationConfig.java
│   │   └── constant/                         # Constantes
│   │       └── ApiConstants.java
│   └── resources/
│       ├── application.properties            # Configurações
│       ├── static/                           # Arquivos estáticos
│       └── templates/                        # Templates (se houver)
└── test/
    └── java/br/com/mikrotik/
        └── MikrotikApplicationTests.java
```

## 🧪 Testes

### Executar Todos os Testes
```bash
mvn test
```

### Executar Teste Específico
```bash
mvn test -Dtest=NomeDaClasseTest
```

### Testes com Cobertura
```bash
mvn clean test jacoco:report
# Relatório em: target/site/jacoco/index.html
```

### Teste de Integração
```bash
mvn verify
```

## 💡 Padrões e Convenções

### Nomenclatura
- **Controllers**: `NomeController` (ex: `UserController`)
- **Services**: `NomeService` (ex: `UserService`)
- **Repositories**: `NomeRepository` (ex: `UserRepository`)
- **DTOs**: `NomeDTO` (ex: `UserDTO`)
- **Entities**: `Nome` (ex: `User`)

### Endpoints
- **GET** - Recuperar recurso
- **POST** - Criar novo recurso
- **PUT** - Atualizar recurso existente
- **DELETE** - Remover recurso
- **PATCH** - Atualização parcial (opcional)

### Validações
- Usar `@NotNull`, `@NotBlank`, `@Email`, etc. nas DTOs
- Validar em nível de serviço também
- Lançar exceções apropriadas

### Logging
```java
log.info("Mensagem informativa");
log.warn("Aviso");
log.error("Erro", exception);
log.debug("Debug");
```

## 🔄 Fluxo de Trabalho Git

### 1. Criar Branch
```bash
git checkout -b feature/minha-funcionalidade
```

### 2. Fazer Alterações
```bash
# Editar arquivos conforme necessário
```

### 3. Committar Mudanças
```bash
git add .
git commit -m "feat: descrição clara da mudança"
```

### 4. Push para Repositório
```bash
git push origin feature/minha-funcionalidade
```

### 5. Criar Pull Request
Através da interface do GitHub/GitLab

## 📝 Adicionando Nova Funcionalidade

### Exemplo: Adicionar Novo Endpoint

#### 1. Criar DTO
```java
// src/main/java/br/com/mikrotik/dto/NovoDTO.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NovoDTO {
    private Long id;
    @NotBlank(message = "Campo obrigatório")
    private String campo;
}
```

#### 2. Criar Entidade
```java
// src/main/java/br/com/mikrotik/model/Novo.java
@Entity
@Table(name = "novos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Novo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String campo;
}
```

#### 3. Criar Repository
```java
// src/main/java/br/com/mikrotik/repository/NovoRepository.java
@Repository
public interface NovoRepository extends JpaRepository<Novo, Long> {
    Optional<Novo> findByCampo(String campo);
}
```

#### 4. Criar Service
```java
// src/main/java/br/com/mikrotik/service/NovoService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class NovoService {
    private final NovoRepository repository;
    
    public NovoDTO create(NovoDTO dto) {
        Novo novo = new Novo();
        // ... lógica
        Novo saved = repository.save(novo);
        return mapToDTO(saved);
    }
}
```

#### 5. Criar Controller
```java
// src/main/java/br/com/mikrotik/controller/NovoController.java
@RestController
@RequestMapping("/api/novo")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Novo", description = "Gerenciar novo recurso")
public class NovoController {
    private final NovoService service;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo")
    public ResponseEntity<NovoDTO> create(@Valid @RequestBody NovoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }
}
```

## 🐛 Debugging

### Via IDE
1. Adicionar breakpoint (clique na linha)
2. Executar em modo Debug (Shift+F9 no IntelliJ)
3. Navegar com F6 (step over) ou F7 (step into)

### Via Logs
```bash
# Aumentar log level em application.properties.txt
logging.level.br.com.mikrotik=DEBUG
logging.level.org.springframework=DEBUG

# Ver logs em tempo real
tail -f logs/application.log
```

### Via Postman/Insomnia
- Importar coleção
- Adicionar breakpoints
- Executar com modo debug ativo

## 🔧 Troubleshooting

### Erro: "No qualifying bean of type 'NomeService'"
- Verificar se classe tem `@Service`
- Verificar se está no pacote correto
- Limpar e recompilar: `mvn clean compile`

### Erro: "Access denied for user 'root'"
- Verificar credenciais em `application.properties`
- Verificar se MySQL está rodando: `docker-compose ps`

### Erro: "Port 8080 is already in use"
```bash
# Encontrar processo
lsof -i :8080
# Matar processo
kill -9 <PID>
# Ou alterar porta em application.properties.txt
server.port=8081
```

### Erro: "Token JWT inválido"
- Fazer novo login
- Verificar expiração em `jwt.expiration`
- Verificar chave secreta em `jwt.secret`

## 📚 Recursos Úteis

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [JWT](https://jwt.io/)
- [Swagger/OpenAPI](https://swagger.io/)

## 👥 Contribuindo

1. Fork o repositório
2. Criar branch para sua feature
3. Fazer commits com mensagens claras
4. Push e criar Pull Request
5. Aguardar review

### Commit Messages
```
feat: nova funcionalidade
fix: correção de bug
refactor: reorganização de código
docs: alterações em documentação
test: adição de testes
```

## 📞 Suporte

Para dúvidas:
1. Verificar issues existentes
2. Criar nova issue com detalhes
3. Contactar desenvolvedor principal

---

**Última atualização:** Janeiro 2026
**Versão:** 1.0.0
