package br.com.mikrotik.features.invoices.job;

import br.com.mikrotik.features.invoices.dto.BillingResultDTO;
import br.com.mikrotik.features.invoices.dto.InvoiceDTO;
import br.com.mikrotik.features.companies.model.Company;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.companies.repository.CompanyRepository;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.invoices.model.Invoice;
import br.com.mikrotik.features.invoices.repository.InvoiceRepository;
import br.com.mikrotik.features.contracts.service.ContractService;
import br.com.mikrotik.features.invoices.service.BillingService;
import br.com.mikrotik.features.invoices.service.InvoiceService;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceBillingJob {

    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final ContractService contractService;
    private final BillingService billingService;

    /**
     * Job que roda todo dia 1º de cada mês às 01:00 AM
     * Gera faturas para todos os contratos ativos
     */
    @Scheduled(cron = "0 0 1 1 * ?", zone = "America/Sao_Paulo") // Dia 1º de cada mês às 01:00 (horário de Brasília)
    public void generateMonthlyInvoices() {
        log.info("========================================");
        log.info("INICIANDO GERAÇÃO AUTOMÁTICA DE FATURAS");
        log.info("Data/Hora: {}", LocalDateTime.now());
        log.info("========================================");

        try {
            // Buscar todas as empresas ativas
            companyRepository.findAll().forEach(company -> {
                if (company.getActive()) {
                    generateInvoicesForCompany(company.getId());
                }
            });

            log.info("========================================");
            log.info("GERAÇÃO DE FATURAS CONCLUÍDA COM SUCESSO");
            log.info("========================================");
        } catch (Exception e) {
            log.error("ERRO ao gerar faturas automáticas: {}", e.getMessage(), e);
        }
    }

    /**
     * Delega para BillingService — evita duplicação de lógica.
     * O contexto da empresa é configurado antes da chamada para garantir
     * multi-tenancy correto nos repositórios que usam CompanyContextHolder.
     */
    private void generateInvoicesForCompany(Long companyId) {
        CompanyContextHolder.setCompanyId(companyId);
        try {
            BillingResultDTO result = billingService.generateMonthlyInvoices(companyId);
            log.info("Empresa #{}: criadas={} ignoradas={} erros={}",
                    companyId, result.created(), result.skipped(), result.errors());
            if (!result.errorDetails().isEmpty()) {
                result.errorDetails().forEach(detail ->
                        log.error("  → Erro: {}", detail));
            }
        } finally {
            CompanyContextHolder.clear();
        }
    }


    /**
     * Job que roda diariamente às 02:00 AM
     * Atualiza status de faturas vencidas para OVERDUE
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "America/Sao_Paulo") // Todo dia às 02:00 (horário de Brasília)
    public void updateOverdueInvoices() {
        log.info("========================================");
        log.info("ATUALIZANDO STATUS DE FATURAS VENCIDAS");
        log.info("Data/Hora: {}", LocalDateTime.now());
        log.info("========================================");

        try {
            companyRepository.findAll().forEach(company -> {
                if (company.getActive()) {
                    updateOverdueInvoicesForCompany(company.getId());
                }
            });

            log.info("========================================");
            log.info("ATUALIZAÇÃO DE FATURAS VENCIDAS CONCLUÍDA");
            log.info("========================================");
        } catch (Exception e) {
            log.error("ERRO ao atualizar faturas vencidas: {}", e.getMessage(), e);
        }
    }

    /**
     * Atualiza faturas vencidas de uma empresa
     */
    private void updateOverdueInvoicesForCompany(Long companyId) {
        CompanyContextHolder.setCompanyId(companyId);

        try {
            // Buscar todas as faturas vencidas (sem paginação)
            Page<InvoiceDTO> overdueInvoicesPage = invoiceService.findOverdue(Pageable.unpaged());
            List<InvoiceDTO> overdueInvoices = overdueInvoicesPage.getContent();

            int count = 0;
            for (InvoiceDTO invoice : overdueInvoices) {
                if (invoice.getStatus().name().equals("PENDING")) {
                    invoiceService.updateStatus(invoice.getId(),
                        Invoice.InvoiceStatus.OVERDUE);
                    count++;
                }
            }

            log.info("Empresa {}: {} faturas marcadas como vencidas", companyId, count);

        } finally {
            CompanyContextHolder.clear();
        }
    }

    /**
     * Job que roda diariamente às 03:00 AM
     * Suspende automaticamente contratos com faturas vencidas há X dias
     * X = suspension_days configurado na empresa (padrão: 5 dias)
     */
    @Scheduled(cron = "0 0 3 * * ?", zone = "America/Sao_Paulo") // Todo dia às 03:00 (horário de Brasília)
    public void suspendOverdueContracts() {
        log.info("==========================================================");
        log.info("SUSPENSÃO AUTOMÁTICA DE CONTRATOS POR INADIMPLÊNCIA");
        log.info("Data/Hora: {}", LocalDateTime.now());
        log.info("==========================================================");

        try {
            companyRepository.findAll().forEach(company -> {
                if (company.getActive()) {
                    suspendOverdueContractsForCompany(company);
                }
            });

            log.info("==========================================================");
            log.info("SUSPENSÃO AUTOMÁTICA CONCLUÍDA");
            log.info("==========================================================");
        } catch (Exception e) {
            log.error("ERRO ao executar suspensão automática: {}", e.getMessage(), e);
        }
    }

    /**
     * Suspende contratos com faturas vencidas de uma empresa
     */
    private void suspendOverdueContractsForCompany(Company company) {
        Long companyId = company.getId();
        Integer suspensionDays = company.getSuspensionDays() != null ? company.getSuspensionDays() : 5;

        log.info("----------------------------------------------------------");
        log.info("Processando empresa: {} (ID: {})", company.getName(), companyId);
        log.info("Dias de tolerância configurados: {} dias", suspensionDays);
        log.info("----------------------------------------------------------");

        CompanyContextHolder.setCompanyId(companyId);

        try {
            LocalDate today = LocalDate.now();
            LocalDate suspensionDate = today.minusDays(suspensionDays);

            log.info("Data atual: {}", today);
            log.info("Data limite para suspensão: {} (faturas vencidas até esta data serão suspensas)", suspensionDate);

            // Buscar contratos com faturas vencidas há X dias ou mais
            List<Long> contractIdsToSuspend = invoiceRepository.findContractIdsForSuspension(companyId, suspensionDate);

            log.info("Encontrados {} contratos para suspensão", contractIdsToSuspend.size());

            int successCount = 0;
            int errorCount = 0;
            int alreadySuspendedCount = 0;

            for (Long contractId : contractIdsToSuspend) {
                try {
                    // Buscar contrato para verificar status atual
                    Contract contract = contractRepository.findByIdAndCompanyId(contractId, companyId)
                            .orElse(null);

                    if (contract == null) {
                        log.warn("  ⚠️  Contrato {} não encontrado", contractId);
                        errorCount++;
                        continue;
                    }

                    // Verificar se já está suspenso
                    if (contract.getStatus() == Contract.ContractStatus.SUSPENDED_FINANCIAL) {
                        log.debug("  ℹ️  Contrato {} já está suspenso - pulando", contractId);
                        alreadySuspendedCount++;
                        continue;
                    }

                    // Verificar se está ativo (só suspende contratos ativos)
                    if (contract.getStatus() != Contract.ContractStatus.ACTIVE) {
                        log.debug("  ℹ️  Contrato {} não está ativo (status: {}) - pulando",
                                contractId, contract.getStatus());
                        continue;
                    }

                    // Suspender contrato (já bloqueia no Mikrotik automaticamente)
                    log.info("  🔒 Suspendendo contrato {} - Cliente: {}",
                            contractId,
                            contract.getCustomer() != null ? contract.getCustomer().getName() : "N/A");

                    contractService.suspendFinancial(contractId);
                    successCount++;

                    log.info("  ✅ Contrato {} suspenso e bloqueado no Mikrotik com sucesso", contractId);

                } catch (Exception e) {
                    errorCount++;
                    log.error("  ❌ Erro ao suspender contrato {}: {}", contractId, e.getMessage());
                }
            }

            log.info("----------------------------------------------------------");
            log.info("Empresa {}: Resumo da suspensão automática", company.getName());
            log.info("  • Contratos para processar: {}", contractIdsToSuspend.size());
            log.info("  • ✅ Suspensos com sucesso: {}", successCount);
            log.info("  • ℹ️  Já estavam suspensos: {}", alreadySuspendedCount);
            log.info("  • ❌ Erros: {}", errorCount);
            log.info("----------------------------------------------------------");

        } catch (Exception e) {
            log.error("ERRO ao processar empresa {}: {}", companyId, e.getMessage(), e);
        } finally {
            CompanyContextHolder.clear();
        }
    }
}
