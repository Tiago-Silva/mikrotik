package br.com.mikrotik.features.invoices.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.companies.repository.CompanyRepository;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.contracts.service.ContractService;
import br.com.mikrotik.features.invoices.job.InvoiceBillingJob;
import br.com.mikrotik.features.invoices.repository.InvoiceRepository;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Endpoints para disparo manual dos jobs de billing.
 *
 * USO PRINCIPAL: testar o fluxo completo sem esperar o horário do scheduler.
 *
 * SEGURANÇA:
 * - Todos exigem INVOICES + EXECUTE
 * - dry_run=true (padrão em suspend) → zero escrita no banco, zero ação no Mikrotik
 * - dry_run=false → executa de verdade
 */
@RestController
@RequestMapping("/api/billing/jobs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Billing Jobs", description = "Disparo manual dos jobs de faturamento e suspensão")
public class BillingJobController {

    private final CompanyRepository companyRepository;
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final InvoiceBillingJob billingJob;

    // ─────────────────────────────────────────────────────────────────────────
    // JOB 1 — Gerar faturas mensais (mesmo do dia 1º às 01:00)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/generate-invoices")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EXECUTE)
    @Operation(
            summary = "Disparar job de geração de faturas",
            description = "Executa imediatamente o job que roda todo dia 1º às 01:00. " +
                          "Idempotente — contratos que já têm fatura no mês são ignorados."
    )
    public ResponseEntity<Map<String, Object>> triggerGenerateInvoices() {
        log.info("▶ [MANUAL] generate-invoices disparado");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("job", "generate-invoices");
        response.put("triggeredAt", LocalDateTime.now().toString());

        billingJob.generateMonthlyInvoices();
        response.put("message", "Job executado — verifique os logs para o resultado detalhado");

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JOB 2 — Marcar faturas vencidas como OVERDUE (mesmo do 02:00)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/mark-overdue")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EXECUTE)
    @Operation(
            summary = "Disparar job de marcação OVERDUE",
            description = "Executa imediatamente o job que roda todo dia às 02:00. " +
                          "dry_run=true mostra quantas faturas seriam marcadas SEM alterar nada no banco."
    )
    public ResponseEntity<Map<String, Object>> triggerMarkOverdue(
            @Parameter(description = "true = apenas simula | false = executa de verdade")
            @RequestParam(defaultValue = "false") boolean dryRun) {

        Long companyId = CompanyContextHolder.getCompanyId();
        log.info("▶ [MANUAL] mark-overdue — empresa={} dryRun={}", companyId, dryRun);

        LocalDate today = LocalDate.now();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("job", "mark-overdue");
        response.put("triggeredAt", LocalDateTime.now().toString());
        response.put("dryRun", dryRun);
        response.put("today", today.toString());

        if (dryRun) {
            long count = invoiceRepository
                    .findOverdueInvoices(companyId, today, Pageable.unpaged())
                    .stream()
                    .filter(i -> i.getStatus() == br.com.mikrotik.features.invoices.model.Invoice.InvoiceStatus.PENDING)
                    .count();
            response.put("wouldMarkOverdue", count);
            response.put("message", "DRY RUN — nenhuma fatura foi alterada");
        } else {
            int count = invoiceRepository.markOverduePendingInvoices(companyId, today);
            response.put("markedOverdue", count);
            response.put("message", count + " faturas marcadas como OVERDUE");
        }

        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JOB 3 — Suspender contratos inadimplentes (mesmo do 03:00)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/suspend-overdue")
    @RequireModuleAccess(module = SystemModule.INVOICES, action = ModuleAction.EXECUTE)
    @Operation(
            summary = "Disparar job de suspensão automática",
            description = "Executa imediatamente o job que roda todo dia às 03:00.\n\n" +
                          "**dry_run=true (padrão):** lista os contratos que seriam suspensos " +
                          "SEM suspender e SEM acionar o Mikrotik. Use para validar antes de executar.\n\n" +
                          "**dry_run=false:** suspende no banco E aciona o Mikrotik."
    )
    public ResponseEntity<Map<String, Object>> triggerSuspendOverdue(
            @Parameter(description = "true = apenas simula (padrão) | false = executa de verdade")
            @RequestParam(defaultValue = "true") boolean dryRun) {

        Long companyId = CompanyContextHolder.getCompanyId();
        log.info("▶ [MANUAL] suspend-overdue — empresa={} dryRun={}", companyId, dryRun);

        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada: " + companyId));

        int suspensionDays = company.getSuspensionDays() != null ? company.getSuspensionDays() : 5;
        LocalDate today          = LocalDate.now();
        LocalDate suspensionDate = today.minusDays(suspensionDays);

        List<Long> contractIds = invoiceRepository.findContractIdsForSuspension(companyId, suspensionDate);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("job", "suspend-overdue");
        response.put("triggeredAt", LocalDateTime.now().toString());
        response.put("dryRun", dryRun);
        response.put("today", today.toString());
        response.put("suspensionDays", suspensionDays);
        response.put("suspensionCutoffDate", suspensionDate.toString());
        response.put("contractsFound", contractIds.size());

        if (dryRun) {
            // ── Apenas lista — zero banco, zero Mikrotik ──────────────────────
            List<Map<String, Object>> preview = new ArrayList<>();

            for (Long contractId : contractIds) {
                contractRepository.findByIdAndCompanyId(contractId, companyId).ifPresent(c -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("contractId", c.getId());
                    item.put("customerId", c.getCustomerId());
                    item.put("currentStatus", c.getStatus().name());
                    item.put("wouldSuspend", c.getStatus() == Contract.ContractStatus.ACTIVE);
                    preview.add(item);
                });
            }

            long wouldSuspend = preview.stream()
                    .filter(p -> Boolean.TRUE.equals(p.get("wouldSuspend")))
                    .count();

            response.put("preview", preview);
            response.put("wouldSuspend", wouldSuspend);
            response.put("message", "DRY RUN — nenhum contrato suspenso, Mikrotik NÃO foi acionado");

        } else {
            // ── Executa de verdade ────────────────────────────────────────────
            int success = 0, skipped = 0, errors = 0;
            List<String> errorDetails = new ArrayList<>();

            for (Long contractId : contractIds) {
                try {
                    Contract contract = contractRepository
                            .findByIdAndCompanyId(contractId, companyId).orElse(null);

                    if (contract == null) {
                        errors++;
                        continue;
                    }
                    if (contract.getStatus() == Contract.ContractStatus.SUSPENDED_FINANCIAL) {
                        skipped++;
                        continue;
                    }
                    if (contract.getStatus() != Contract.ContractStatus.ACTIVE) {
                        skipped++;
                        continue;
                    }

                    contractService.suspendFinancial(contractId);
                    success++;
                    log.info("  ✅ Contrato {} suspenso", contractId);

                } catch (Exception e) {
                    errors++;
                    errorDetails.add("Contrato #" + contractId + ": " + e.getMessage());
                    log.error("  ❌ Erro ao suspender contrato {}: {}", contractId, e.getMessage());
                }
            }

            response.put("suspended", success);
            response.put("skipped", skipped);
            response.put("errors", errors);
            response.put("errorDetails", errorDetails);
        }

        return ResponseEntity.ok(response);
    }
}

