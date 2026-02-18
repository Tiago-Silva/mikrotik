package br.com.mikrotik.features.financial.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.financial.dto.TransactionDTO;
import br.com.mikrotik.features.financial.model.Transaction;
import br.com.mikrotik.features.financial.service.TransactionService;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Transações", description = "Gerenciamento de transações de pagamento")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.CREATE)
    @Operation(summary = "Registrar pagamento", description = "Cria uma nova transação e marca a fatura como paga")
    public ResponseEntity<TransactionDTO> create(@Valid @RequestBody TransactionDTO dto) {
        log.info("POST /api/transactions - Registrando pagamento");
        TransactionDTO created = transactionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar transação por ID", description = "Retorna detalhes de uma transação específica")
    public ResponseEntity<TransactionDTO> findById(@PathVariable Long id) {
        log.info("GET /api/transactions/{} - Buscando transação", id);
        TransactionDTO transaction = transactionService.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/invoice/{invoiceId}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar transações por fatura", description = "Lista todas as transações de uma fatura (paginado)")
    public ResponseEntity<Page<TransactionDTO>> findByInvoice(
            @PathVariable Long invoiceId,
            @PageableDefault(size = 20, sort = "paidAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/invoice/{} - Listando transações", invoiceId);
        Page<TransactionDTO> transactions = transactionService.findByInvoice(invoiceId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/method/{method}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar por método", description = "Lista transações por método de pagamento")
    public ResponseEntity<Page<TransactionDTO>> findByMethod(
            @PathVariable Transaction.PaymentMethod method,
            @PageableDefault(size = 20, sort = "paidAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/method/{} - Listando transações", method);
        Page<TransactionDTO> transactions = transactionService.findByMethod(method, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/period")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar por período", description = "Lista transações em um período específico (paginado)")
    public ResponseEntity<Page<TransactionDTO>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 50, sort = "paidAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/period?startDate={}&endDate={}", startDate, endDate);
        Page<TransactionDTO> transactions = transactionService.findByPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/filter")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar com filtros", description = "Busca transações com múltiplos filtros opcionais")
    public ResponseEntity<Page<TransactionDTO>> findByFilters(
            @RequestParam(required = false) Long invoiceId,
            @RequestParam(required = false) Transaction.PaymentMethod method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "paidAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/filter - Buscando com filtros");
        Page<TransactionDTO> transactions = transactionService.findByFilters(invoiceId, method, startDate, endDate, pageable);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação")
    public ResponseEntity<TransactionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto) {
        log.info("PUT /api/transactions/{} - Atualizando transação", id);
        TransactionDTO updated = transactionService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.DELETE)
    @Operation(summary = "Deletar transação", description = "Remove uma transação do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/transactions/{} - Deletando transação", id);
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/method/{method}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Contar por método", description = "Retorna o número de transações por método de pagamento")
    public ResponseEntity<Long> countByMethod(@PathVariable Transaction.PaymentMethod method) {
        log.info("GET /api/transactions/count/method/{}", method);
        long count = transactionService.countByMethod(method);
        return ResponseEntity.ok(count);
    }
}
