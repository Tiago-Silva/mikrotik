package br.com.mikrotik.job;

import br.com.mikrotik.dto.InvoiceDTO;
import br.com.mikrotik.model.Contract;
import br.com.mikrotik.repository.CompanyRepository;
import br.com.mikrotik.repository.ContractRepository;
import br.com.mikrotik.service.InvoiceService;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceBillingJob {

    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;
    private final InvoiceService invoiceService;

    /**
     * Job que roda todo dia 1º de cada mês às 01:00 AM
     * Gera faturas para todos os contratos ativos
     */
    @Scheduled(cron = "0 0 1 1 * ?") // Dia 1º de cada mês às 01:00
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
     * Gera faturas para uma empresa específica
     */
    private void generateInvoicesForCompany(Long companyId) {
        log.info("Gerando faturas para empresa ID: {}", companyId);

        // Definir contexto da empresa
        CompanyContextHolder.setCompanyId(companyId);

        try {
            int currentDay = LocalDate.now().getDayOfMonth();

            // Buscar contratos ativos para faturamento (todos os registros)
            Page<Contract> contractsPage = contractRepository.findContractsForBilling(
                companyId, currentDay, Pageable.unpaged());
            List<Contract> contracts = contractsPage.getContent();

            log.info("Encontrados {} contratos para faturamento na empresa {}", contracts.size(), companyId);

            int successCount = 0;
            int errorCount = 0;

            for (Contract contract : contracts) {
                try {
                    generateInvoiceForContract(contract);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.error("Erro ao gerar fatura para contrato {}: {}", contract.getId(), e.getMessage());
                }
            }

            log.info("Empresa {}: {} faturas geradas com sucesso, {} erros",
                    companyId, successCount, errorCount);

        } finally {
            // Limpar contexto
            CompanyContextHolder.clear();
        }
    }

    /**
     * Gera uma fatura para um contrato específico
     */
    private void generateInvoiceForContract(Contract contract) {
        LocalDate today = LocalDate.now();
        LocalDate referenceMonth = today.withDayOfMonth(1); // Primeiro dia do mês atual
        LocalDate dueDate = today.withDayOfMonth(contract.getBillingDay());

        // Se o dia de vencimento já passou neste mês, usar próximo mês
        if (dueDate.isBefore(today)) {
            dueDate = dueDate.plusMonths(1);
        }

        // Montar descrição
        String monthYear = String.format("%02d/%d", today.getMonthValue(), today.getYear());
        String description = String.format("Mensalidade Internet - %s", monthYear);

        // Criar DTO da fatura
        InvoiceDTO invoiceDTO = InvoiceDTO.builder()
                .companyId(contract.getCompanyId())
                .contractId(contract.getId())
                .customerId(contract.getCustomerId())
                .description(description)
                .referenceMonth(referenceMonth)
                .dueDate(dueDate)
                .originalAmount(contract.getAmount())
                .discountAmount(BigDecimal.ZERO)
                .interestAmount(BigDecimal.ZERO)
                .finalAmount(contract.getAmount())
                .build();

        // Criar fatura
        InvoiceDTO createdInvoice = invoiceService.create(invoiceDTO);

        log.info("Fatura {} gerada para contrato {} - Cliente: {} - Valor: R$ {}",
                createdInvoice.getId(),
                contract.getId(),
                contract.getCustomer() != null ? contract.getCustomer().getName() : "N/A",
                createdInvoice.getFinalAmount());
    }

    /**
     * Job que roda diariamente às 02:00 AM
     * Atualiza status de faturas vencidas para OVERDUE
     */
    @Scheduled(cron = "0 0 2 * * ?") // Todo dia às 02:00
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
                        br.com.mikrotik.model.Invoice.InvoiceStatus.OVERDUE);
                    count++;
                }
            }

            log.info("Empresa {}: {} faturas marcadas como vencidas", companyId, count);

        } finally {
            CompanyContextHolder.clear();
        }
    }
}
