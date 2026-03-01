package br.com.mikrotik.features.invoices.service;

import br.com.mikrotik.features.companies.repository.CompanyRepository;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.invoices.dto.BillingResultDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de faturamento mensal em lote.
 *
 * RESPONSABILIDADE: Orquestrar a geração de faturas para todos os contratos ACTIVE
 * de uma empresa. Não gerencia transações diretamente — delega ao BillingInvoiceCreator,
 * que garante uma transação isolada por contrato (evita self-invocation e falha em cascata).
 *
 * FLUXO:
 *  1. Validar empresa existe e está ativa.
 *  2. Buscar todos os contratos ACTIVE da empresa.
 *  3. Para cada contrato delegar ao BillingInvoiceCreator:
 *     a. Verificar idempotência: se fatura do mês já existe → SKIP.
 *     b. Persistir Invoice PENDING em transação própria.
 *     c. Publicar InvoiceCreatedEvent (após commit).
 *  4. AbacatePayEventListener consome o evento assincronamente e grava o link PIX.
 *
 * REGRA DE OURO: A chamada ao AbacatePay NUNCA ocorre dentro de @Transactional.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final ContractRepository contractRepository;
    private final CompanyRepository companyRepository;
    private final BillingInvoiceCreator invoiceCreator;

    private static final DateTimeFormatter MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Gera faturas mensais para todos os contratos ACTIVE de uma empresa.
     * Idempotente: contratos que já possuem fatura no mês vigente são ignorados.
     *
     * @param companyId ID da empresa (multi-tenant)
     * @return Resultado detalhado da operação (criados, ignorados, erros)
     */
    public BillingResultDTO generateMonthlyInvoices(Long companyId) {
        log.info("========================================================");
        log.info("INICIANDO GERAÇÃO DE FATURAS EM LOTE — empresa #{}", companyId);
        log.info("========================================================");

        companyRepository.findById(companyId)
                .filter(c -> Boolean.TRUE.equals(c.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Empresa não encontrada ou inativa: " + companyId));

        LocalDate today         = LocalDate.now();
        LocalDate referenceMonth = today.withDayOfMonth(1);

        List<Contract> activeContracts =
                contractRepository.findByCompanyIdAndStatus(companyId, Contract.ContractStatus.ACTIVE);

        log.info("Contratos ACTIVE encontrados: {}", activeContracts.size());

        int created = 0;
        int skipped = 0;
        int errors  = 0;
        List<String> errorDetails = new ArrayList<>();

        for (Contract contract : activeContracts) {
            try {
                boolean wasCreated = invoiceCreator.createIfAbsent(contract, referenceMonth, today);
                if (wasCreated) {
                    created++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                errors++;
                String detail = String.format("Contrato #%d (cliente #%d): %s",
                        contract.getId(), contract.getCustomerId(), e.getMessage());
                errorDetails.add(detail);
                log.error("Erro ao gerar fatura para contrato #{}: {}", contract.getId(), e.getMessage(), e);
            }
        }

        String referenceMonthLabel = referenceMonth.format(MONTH_FORMATTER);
        log.info("========================================================");
        log.info("GERAÇÃO CONCLUÍDA — empresa #{} mês={}  criadas={} ignoradas={} erros={}",
                companyId, referenceMonthLabel, created, skipped, errors);
        log.info("========================================================");

        return new BillingResultDTO(companyId, referenceMonthLabel, created, skipped, errors, errorDetails);
    }
}
