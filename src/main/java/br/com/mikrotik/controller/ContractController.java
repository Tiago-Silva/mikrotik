package br.com.mikrotik.controller;

import br.com.mikrotik.dto.ContractDTO;
import br.com.mikrotik.model.Contract;
import br.com.mikrotik.service.ContractService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Contratos", description = "Gerenciamento de contratos de serviço")
public class ContractController {

    private final ContractService contractService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar novo contrato", description = "Cria um novo contrato de serviço")
    public ResponseEntity<ContractDTO> create(@Valid @RequestBody ContractDTO dto) {
        log.info("POST /api/contracts - Criando novo contrato");
        ContractDTO created = contractService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar contrato por ID", description = "Retorna detalhes de um contrato específico")
    public ResponseEntity<ContractDTO> findById(@PathVariable Long id) {
        log.info("GET /api/contracts/{} - Buscando contrato", id);
        ContractDTO contract = contractService.findById(id);
        return ResponseEntity.ok(contract);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos os contratos", description = "Lista contratos da empresa (paginado)")
    public ResponseEntity<Page<ContractDTO>> findAll(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/contracts - Listando contratos");
        Page<ContractDTO> contracts = contractService.findAll(pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar contratos por cliente", description = "Lista contratos de um cliente específico")
    public ResponseEntity<Page<ContractDTO>> findByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/contracts/customer/{} - Listando contratos", customerId);
        Page<ContractDTO> contracts = contractService.findByCustomer(customerId, pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar contratos por status", description = "Lista contratos filtrados por status")
    public ResponseEntity<Page<ContractDTO>> findByStatus(
            @PathVariable Contract.ContractStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/contracts/status/{} - Listando contratos", status);
        Page<ContractDTO> contracts = contractService.findByStatus(status, pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar com filtros", description = "Busca contratos com múltiplos filtros opcionais")
    public ResponseEntity<Page<ContractDTO>> findByFilters(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Contract.ContractStatus status,
            @RequestParam(required = false) Long servicePlanId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/contracts/filter - Buscando com filtros");
        Page<ContractDTO> contracts = contractService.findByFilters(customerId, status, servicePlanId, pageable);
        return ResponseEntity.ok(contracts);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar contrato", description = "Atualiza os dados de um contrato")
    public ResponseEntity<ContractDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ContractDTO dto) {
        log.info("PUT /api/contracts/{} - Atualizando contrato", id);
        ContractDTO updated = contractService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Alterar status", description = "Altera o status de um contrato")
    public ResponseEntity<ContractDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Contract.ContractStatus status) {
        log.info("PATCH /api/contracts/{}/status?status={}", id, status);
        ContractDTO updated = contractService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Ativar contrato", description = "Ativa um contrato (status ACTIVE)")
    public ResponseEntity<ContractDTO> activate(@PathVariable Long id) {
        log.info("PATCH /api/contracts/{}/activate", id);
        ContractDTO updated = contractService.activate(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/suspend-financial")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Suspender por inadimplência", description = "Suspende contrato por falta de pagamento")
    public ResponseEntity<ContractDTO> suspendFinancial(@PathVariable Long id) {
        log.info("PATCH /api/contracts/{}/suspend-financial", id);
        ContractDTO updated = contractService.suspendFinancial(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/suspend-request")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Suspender por solicitação", description = "Suspende contrato por solicitação do cliente")
    public ResponseEntity<ContractDTO> suspendByRequest(@PathVariable Long id) {
        log.info("PATCH /api/contracts/{}/suspend-request", id);
        ContractDTO updated = contractService.suspendByRequest(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Cancelar contrato", description = "Cancela um contrato")
    public ResponseEntity<ContractDTO> cancel(@PathVariable Long id) {
        log.info("PATCH /api/contracts/{}/cancel", id);
        ContractDTO updated = contractService.cancel(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar contrato", description = "Remove um contrato do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/contracts/{} - Deletando contrato", id);
        contractService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/billing/{billingDay}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Buscar contratos para faturamento", description = "Retorna contratos ativos para faturamento no dia específico (paginado)")
    public ResponseEntity<Page<ContractDTO>> findContractsForBilling(
            @PathVariable Integer billingDay,
            @PageableDefault(size = 100, sort = "id") Pageable pageable) {
        log.info("GET /api/contracts/billing/{} - Buscando contratos para faturamento", billingDay);
        Page<ContractDTO> contracts = contractService.findContractsForBilling(billingDay, pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar contratos", description = "Retorna o número total de contratos da empresa")
    public ResponseEntity<Long> countByCompany() {
        log.info("GET /api/contracts/count");
        long count = contractService.countByCompany();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar por status", description = "Retorna o número de contratos por status")
    public ResponseEntity<Long> countByStatus(@PathVariable Contract.ContractStatus status) {
        log.info("GET /api/contracts/count/status/{}", status);
        long count = contractService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
}
