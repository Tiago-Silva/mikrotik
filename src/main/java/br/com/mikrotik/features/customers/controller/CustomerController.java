package br.com.mikrotik.features.customers.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.customers.dto.CustomerDTO;
import br.com.mikrotik.features.customers.model.Customer;
import br.com.mikrotik.features.customers.service.CustomerService;
import br.com.mikrotik.features.network.pppoe.dto.LiveConnectionDTO;
import br.com.mikrotik.features.network.pppoe.service.CustomerMonitoringService;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Clientes", description = "Gerenciamento de clientes (CRM)")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMonitoringService customerMonitoringService;

    @PostMapping
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.CREATE)
    @Operation(summary = "Criar novo cliente", description = "Cria um novo cliente (PF ou PJ) no sistema")
    public ResponseEntity<CustomerDTO> create(@Valid @RequestBody CustomerDTO dto) {
        log.info("POST /api/customers - Criando novo cliente: {}", dto.getName());
        CustomerDTO created = customerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar cliente por ID", description = "Retorna detalhes de um cliente específico")
    public ResponseEntity<CustomerDTO> findById(@PathVariable Long id) {
        log.info("GET /api/customers/{} - Buscando cliente", id);
        CustomerDTO customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/document/{document}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar cliente por documento", description = "Retorna cliente pelo CPF ou CNPJ")
    public ResponseEntity<CustomerDTO> findByDocument(@PathVariable String document) {
        log.info("GET /api/customers/document/{} - Buscando cliente por documento", document);
        CustomerDTO customer = customerService.findByDocument(document);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todos os clientes", description = "Lista todos os clientes da empresa com paginação")
    public ResponseEntity<Page<CustomerDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/customers - Listando clientes");
        Page<CustomerDTO> customers = customerService.findAll(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/status/{status}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar clientes por status", description = "Retorna clientes filtrados por status")
    public ResponseEntity<Page<CustomerDTO>> findByStatus(
            @PathVariable Customer.CustomerStatus status,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/customers/status/{} - Buscando clientes por status", status);
        Page<CustomerDTO> customers = customerService.findByStatus(status, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/type/{type}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar clientes por tipo", description = "Retorna clientes filtrados por tipo (FISICA ou JURIDICA)")
    public ResponseEntity<Page<CustomerDTO>> findByType(
            @PathVariable Customer.CustomerType type,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/customers/type/{} - Buscando clientes por tipo", type);
        Page<CustomerDTO> customers = customerService.findByType(type, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/search")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar clientes por nome", description = "Busca clientes por nome (parcial)")
    public ResponseEntity<Page<CustomerDTO>> findByName(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/customers/search?name={}", name);
        Page<CustomerDTO> customers = customerService.findByName(name, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/city/{city}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar clientes por cidade", description = "Retorna clientes de uma cidade específica")
    public ResponseEntity<Page<CustomerDTO>> findByCity(
            @PathVariable String city,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/customers/city/{} - Buscando clientes por cidade", city);
        Page<CustomerDTO> customers = customerService.findByCity(city, pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/filter")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar clientes com filtros", description = "Busca clientes com múltiplos filtros opcionais")
    public ResponseEntity<Page<CustomerDTO>> findByFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Customer.CustomerStatus status,
            @RequestParam(required = false) Customer.CustomerType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/customers/filter - Buscando com filtros - nome: {}, status: {}, tipo: {}, de: {}, até: {}",
                name, status, type, createdFrom, createdTo);
        Page<CustomerDTO> customers = customerService.findByFilters(name, status, type, createdFrom, createdTo, pageable);
        return ResponseEntity.ok(customers);
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    public ResponseEntity<CustomerDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO dto) {
        log.info("PUT /api/customers/{} - Atualizando cliente", id);
        CustomerDTO updated = customerService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.EDIT)
    @Operation(summary = "Alterar status do cliente", description = "Altera o status de um cliente")
    public ResponseEntity<CustomerDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Customer.CustomerStatus status) {
        log.info("PATCH /api/customers/{}/status?status={} - Alterando status", id, status);
        CustomerDTO updated = customerService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.DELETE)
    @Operation(summary = "Deletar cliente", description = "Remove um cliente do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/customers/{} - Deletando cliente", id);
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/status/{status}")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(summary = "Contar clientes por status", description = "Retorna o número de clientes por status")
    public ResponseEntity<Long> countByStatus(@PathVariable Customer.CustomerStatus status) {
        log.info("GET /api/customers/count/status/{} - Contando clientes", status);
        long count = customerService.countByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{customerId}/live")
    @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
    @Operation(
            summary = "Conexão ativa do cliente (ao vivo)",
            description = "Consulta diretamente o Mikrotik e retorna a sessão PPPoE ativa do cliente. " +
                    "Use para monitoramento sob demanda — não para listagem em massa."
    )
    public ResponseEntity<LiveConnectionDTO> getLiveConnectionByCustomer(@PathVariable Long customerId) {
        log.info("GET /api/connections/customer/{}/live - Consultando sessão ativa no Mikrotik", customerId);
        return ResponseEntity.ok(customerMonitoringService.getLiveConnectionByCustomerId(customerId));
    }
}
