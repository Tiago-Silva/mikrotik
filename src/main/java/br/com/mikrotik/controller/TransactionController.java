package br.com.mikrotik.controller;

import br.com.mikrotik.dto.TransactionDTO;
import br.com.mikrotik.model.Transaction;
import br.com.mikrotik.service.TransactionService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Transações", description = "Gerenciamento de transações de pagamento")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Registrar pagamento", description = "Cria uma nova transação e marca a fatura como paga")
    public ResponseEntity<TransactionDTO> create(@Valid @RequestBody TransactionDTO dto) {
        log.info("POST /api/transactions - Registrando pagamento");
        TransactionDTO created = transactionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar transação por ID", description = "Retorna detalhes de uma transação específica")
    public ResponseEntity<TransactionDTO> findById(@PathVariable Long id) {
        log.info("GET /api/transactions/{} - Buscando transação", id);
        TransactionDTO transaction = transactionService.findById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar transações por fatura", description = "Lista todas as transações de uma fatura")
    public ResponseEntity<List<TransactionDTO>> findByInvoice(@PathVariable Long invoiceId) {
        log.info("GET /api/transactions/invoice/{} - Listando transações", invoiceId);
        List<TransactionDTO> transactions = transactionService.findByInvoice(invoiceId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/method/{method}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar por método", description = "Lista transações por método de pagamento")
    public ResponseEntity<Page<TransactionDTO>> findByMethod(
            @PathVariable Transaction.PaymentMethod method,
            @PageableDefault(size = 20, sort = "paidAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/transactions/method/{} - Listando transações", method);
        Page<TransactionDTO> transactions = transactionService.findByMethod(method, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar por período", description = "Lista transações em um período específico")
    public ResponseEntity<List<TransactionDTO>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /api/transactions/period?startDate={}&endDate={}", startDate, endDate);
        List<TransactionDTO> transactions = transactionService.findByPeriod(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar transação", description = "Atualiza os dados de uma transação")
    public ResponseEntity<TransactionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionDTO dto) {
        log.info("PUT /api/transactions/{} - Atualizando transação", id);
        TransactionDTO updated = transactionService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar transação", description = "Remove uma transação do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/transactions/{} - Deletando transação", id);
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count/method/{method}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar por método", description = "Retorna o número de transações por método de pagamento")
    public ResponseEntity<Long> countByMethod(@PathVariable Transaction.PaymentMethod method) {
        log.info("GET /api/transactions/count/method/{}", method);
        long count = transactionService.countByMethod(method);
        return ResponseEntity.ok(count);
    }
}
