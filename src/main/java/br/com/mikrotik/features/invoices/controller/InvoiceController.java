package br.com.mikrotik.features.invoices.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.invoices.dto.InvoiceDTO;
import br.com.mikrotik.features.invoices.model.Invoice;
import br.com.mikrotik.features.invoices.service.InvoiceService;
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
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Faturas", description = "Gerenciamento de faturas e cobranças")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.CREATE)
    @Operation(summary = "Criar nova fatura", description = "Cria uma nova fatura para um contrato")
    public ResponseEntity<InvoiceDTO> create(@Valid @RequestBody InvoiceDTO dto) {
        log.info("POST /api/invoices - Criando nova fatura");
        InvoiceDTO created = invoiceService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar fatura por ID", description = "Retorna detalhes de uma fatura específica")
    public ResponseEntity<InvoiceDTO> findById(@PathVariable Long id) {
        log.info("GET /api/invoices/{} - Buscando fatura", id);
        InvoiceDTO invoice = invoiceService.findById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todas as faturas", description = "Lista faturas da empresa (paginado)")
    public ResponseEntity<Page<InvoiceDTO>> findAll(
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/invoices - Listando faturas");
        Page<InvoiceDTO> invoices = invoiceService.findAll(pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/customer/{customerId}")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Listar faturas por cliente", description = "Lista faturas de um cliente específico")
    public ResponseEntity<Page<InvoiceDTO>> findByCustomer(
            @PathVariable Long customerId,
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/invoices/customer/{} - Listando faturas", customerId);
        Page<InvoiceDTO> invoices = invoiceService.findByCustomer(customerId, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/status/{status}")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Listar faturas por status", description = "Lista faturas filtradas por status")
    public ResponseEntity<Page<InvoiceDTO>> findByStatus(
            @PathVariable Invoice.InvoiceStatus status,
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/invoices/status/{} - Listando faturas", status);
        Page<InvoiceDTO> invoices = invoiceService.findByStatus(status, pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/overdue")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Listar faturas vencidas", description = "Retorna todas as faturas vencidas e não pagas (paginado)")
    public ResponseEntity<Page<InvoiceDTO>> findOverdue(
            @PageableDefault(size = 50, sort = "dueDate") Pageable pageable) {
        log.info("GET /api/invoices/overdue - Listando faturas vencidas");
        Page<InvoiceDTO> invoices = invoiceService.findOverdue(pageable);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/filter")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar com filtros", description = "Busca faturas com múltiplos filtros opcionais")
    public ResponseEntity<Page<InvoiceDTO>> findByFilters(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Invoice.InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/invoices/filter - Buscando com filtros");
        Page<InvoiceDTO> invoices = invoiceService.findByFilters(customerId, status, month, pageable);
        return ResponseEntity.ok(invoices);
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar fatura", description = "Atualiza os dados de uma fatura")
    public ResponseEntity<InvoiceDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceDTO dto) {
        log.info("PUT /api/invoices/{} - Atualizando fatura", id);
        InvoiceDTO updated = invoiceService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EDIT)
    @Operation(summary = "Alterar status", description = "Altera o status de uma fatura")
    public ResponseEntity<InvoiceDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Invoice.InvoiceStatus status) {
        log.info("PATCH /api/invoices/{}/status?status={}", id, status);
        InvoiceDTO updated = invoiceService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/pay")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EDIT)
    @Operation(summary = "Marcar como paga", description = "Marca uma fatura como paga")
    public ResponseEntity<InvoiceDTO> markAsPaid(@PathVariable Long id) {
        log.info("PATCH /api/invoices/{}/pay", id);
        InvoiceDTO updated = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/cancel")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EDIT)
    @Operation(summary = "Cancelar fatura", description = "Cancela uma fatura")
    public ResponseEntity<InvoiceDTO> cancel(@PathVariable Long id) {
        log.info("PATCH /api/invoices/{}/cancel", id);
        InvoiceDTO updated = invoiceService.cancel(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.DELETE)
    @Operation(summary = "Deletar fatura", description = "Remove uma fatura do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/invoices/{} - Deletando fatura", id);
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Contar faturas", description = "Retorna o número total de faturas da empresa")
    public ResponseEntity<Long> countByCompany() {
        log.info("GET /api/invoices/count");
        long count = invoiceService.countByCompany();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/status/{status}")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.VIEW)
    @Operation(summary = "Contar por status", description = "Retorna o número de faturas por status")
    public ResponseEntity<Long> countByStatus(@PathVariable Invoice.InvoiceStatus status) {
        log.info("GET /api/invoices/count/status/{}", status);
        long count = invoiceService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
}
