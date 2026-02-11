package br.com.mikrotik.controller;

import br.com.mikrotik.dto.FinancialEntryDTO;
import br.com.mikrotik.model.FinancialEntry;
import br.com.mikrotik.service.CashFlowService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/financial-entries")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Lançamentos Financeiros", description = "Gerenciamento de lançamentos financeiros")
public class FinancialEntryController {

    private final CashFlowService cashFlowService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar lançamento", description = "Cria novo lançamento financeiro e atualiza saldo da conta")
    public ResponseEntity<FinancialEntryDTO> create(@Valid @RequestBody FinancialEntryDTO dto) {
        log.info("POST /api/financial-entries - Criando lançamento: {}", dto.getDescription());
        FinancialEntryDTO created = cashFlowService.processEntry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Estornar lançamento", description = "Estorna um lançamento existente")
    public ResponseEntity<FinancialEntryDTO> reverse(@PathVariable Long id) {
        log.info("POST /api/financial-entries/{}/reverse", id);
        FinancialEntryDTO reversal = cashFlowService.reverseEntry(id);
        return ResponseEntity.ok(reversal);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar por ID", description = "Retorna detalhes de um lançamento")
    public ResponseEntity<FinancialEntryDTO> findById(@PathVariable Long id) {
        log.info("GET /api/financial-entries/{}", id);
        FinancialEntryDTO entry = cashFlowService.findById(id);
        return ResponseEntity.ok(entry);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar lançamentos", description = "Lista todos os lançamentos (paginado)")
    public ResponseEntity<Page<FinancialEntryDTO>> findAll(
            @PageableDefault(size = 20, sort = "effectiveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/financial-entries - Listando lançamentos");
        Page<FinancialEntryDTO> entries = cashFlowService.findAll(pageable);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/bank-account/{bankAccountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar por conta", description = "Lista lançamentos de uma conta bancária específica")
    public ResponseEntity<Page<FinancialEntryDTO>> findByBankAccount(
            @PathVariable Long bankAccountId,
            @PageableDefault(size = 20, sort = "effectiveDate", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/financial-entries/bank-account/{}", bankAccountId);
        Page<FinancialEntryDTO> entries = cashFlowService.findByBankAccount(bankAccountId, pageable);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar por período", description = "Lista lançamentos entre duas datas")
    public ResponseEntity<Page<FinancialEntryDTO>> findByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "referenceDate", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/financial-entries/period?startDate={}&endDate={}", startDate, endDate);
        Page<FinancialEntryDTO> entries = cashFlowService.findByPeriod(startDate, endDate, pageable);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/summary/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Resumo por período", description = "Retorna total de entradas e saídas em um período")
    public ResponseEntity<Map<String, BigDecimal>> getSummaryByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /api/financial-entries/summary/period?startDate={}&endDate={}", startDate, endDate);

        BigDecimal totalCredits = cashFlowService.sumByPeriodAndType(startDate, endDate, FinancialEntry.EntryType.CREDIT);
        BigDecimal totalDebits = cashFlowService.sumByPeriodAndType(startDate, endDate, FinancialEntry.EntryType.DEBIT);
        BigDecimal balance = totalCredits.subtract(totalDebits);

        Map<String, BigDecimal> summary = Map.of(
                "totalCredits", totalCredits,
                "totalDebits", totalDebits,
                "balance", balance
        );

        return ResponseEntity.ok(summary);
    }
}

