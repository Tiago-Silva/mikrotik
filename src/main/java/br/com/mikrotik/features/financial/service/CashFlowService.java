package br.com.mikrotik.features.financial.service;

import br.com.mikrotik.features.financial.dto.FinancialEntryDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.infrastructure.exception.ValidationException;
import br.com.mikrotik.features.financial.model.BankAccount;
import br.com.mikrotik.features.financial.model.ChartOfAccounts;
import br.com.mikrotik.features.financial.model.FinancialEntry;
import br.com.mikrotik.features.financial.repository.BankAccountRepository;
import br.com.mikrotik.features.financial.repository.ChartOfAccountsRepository;
import br.com.mikrotik.features.financial.repository.FinancialEntryRepository;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashFlowService {

    private final FinancialEntryRepository financialEntryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;

    /**
     * Processar entrada financeira com atualização de saldo (THREAD-SAFE com Pessimistic Lock)
     */
    @Transactional
    public FinancialEntryDTO processEntry(FinancialEntryDTO dto) {
        log.info("==========================================================");
        log.info(">>> PROCESSANDO LANÇAMENTO FINANCEIRO <<<");
        log.info("==========================================================");

        Long companyId = CompanyContextHolder.getCompanyId();
        dto.setCompanyId(companyId);

        // Validar conta bancária
        BankAccount bankAccount = bankAccountRepository
                .findByIdAndCompanyIdWithLock(dto.getBankAccountId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta bancária não encontrada: " + dto.getBankAccountId()));

        if (!bankAccount.getActive()) {
            throw new ValidationException("Conta bancária está inativa");
        }

        log.info("Conta bancária: {} (Saldo atual: {})", bankAccount.getName(), bankAccount.getCurrentBalance());

        // Validar plano de contas
        ChartOfAccounts chartOfAccount = chartOfAccountsRepository
                .findByIdAndCompanyId(dto.getChartOfAccountId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conta do plano de contas não encontrada: " + dto.getChartOfAccountId()));

        if (!chartOfAccount.getActive()) {
            throw new ValidationException("Conta do plano de contas está inativa");
        }

        log.info("Plano de contas: {} ({})", chartOfAccount.getName(), chartOfAccount.getCategory());

        // Definir data efetiva se não informada
        if (dto.getEffectiveDate() == null) {
            dto.setEffectiveDate(LocalDateTime.now());
        }

        // Definir data de competência se não informada
        if (dto.getReferenceDate() == null) {
            dto.setReferenceDate(LocalDate.now());
        }

        // Definir status padrão
        if (dto.getStatus() == null) {
            dto.setStatus(FinancialEntry.Status.ACTIVE);
        }

        // Salvar lançamento
        FinancialEntry entry = dto.toEntity();
        entry = financialEntryRepository.save(entry);
        log.info("Lançamento salvo: ID={}, Tipo={}, Valor={}",
                entry.getId(), entry.getEntryType(), entry.getAmount());

        // Atualizar saldo da conta bancária (LOCK JÁ ADQUIRIDO)
        BigDecimal oldBalance = bankAccount.getCurrentBalance();
        BigDecimal newBalance;

        if (dto.getEntryType() == FinancialEntry.EntryType.CREDIT) {
            newBalance = oldBalance.add(dto.getAmount());
            log.info("CRÉDITO: {} + {} = {}", oldBalance, dto.getAmount(), newBalance);
        } else if (dto.getEntryType() == FinancialEntry.EntryType.DEBIT) {
            newBalance = oldBalance.subtract(dto.getAmount());
            log.info("DÉBITO: {} - {} = {}", oldBalance, dto.getAmount(), newBalance);

            // Validar saldo negativo (opcional - ISP pode permitir)
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("⚠️  ATENÇÃO: Saldo ficará negativo: {}", newBalance);
            }
        } else {
            // REVERSAL - não altera saldo, apenas marca como estornado
            newBalance = oldBalance;
            log.info("ESTORNO: Saldo mantido em {}", oldBalance);
        }

        bankAccount.setCurrentBalance(newBalance);
        bankAccountRepository.save(bankAccount);

        log.info("==========================================================");
        log.info("✅ SALDO ATUALIZADO: {} → {}", oldBalance, newBalance);
        log.info("==========================================================");

        return FinancialEntryDTO.fromEntity(entry);
    }

    /**
     * Processar pagamento de fatura (chamado pelo EventListener)
     */
    @Transactional
    public FinancialEntryDTO processInvoicePayment(Long invoiceId, Long companyId,
                                                   BigDecimal amount, LocalDateTime paidAt) {
        log.info(">>> PROCESSANDO PAGAMENTO DE FATURA: invoiceId={}, amount={}", invoiceId, amount);

        // Buscar conta bancária padrão (caixa)
        BankAccount defaultAccount = bankAccountRepository.findDefaultCashAccount(companyId)
                .orElseThrow(() -> new ValidationException(
                        "Nenhuma conta bancária padrão (CASH) encontrada. Configure uma conta CASH ativa."));

        // Buscar conta do plano de contas para receita de assinatura
        ChartOfAccounts revenueAccount = chartOfAccountsRepository
                .findDefaultSubscriptionRevenueAccount(companyId)
                .orElseThrow(() -> new ValidationException(
                        "Nenhuma conta de receita de assinatura encontrada. Configure uma conta SUBSCRIPTION_REVENUE ativa."));

        // Criar lançamento
        FinancialEntryDTO dto = FinancialEntryDTO.builder()
                .companyId(companyId)
                .bankAccountId(defaultAccount.getId())
                .chartOfAccountId(revenueAccount.getId())
                .entryType(FinancialEntry.EntryType.CREDIT)
                .transactionType(FinancialEntry.TransactionType.INVOICE_PAYMENT)
                .amount(amount)
                .description("Pagamento de fatura #" + invoiceId)
                .referenceDate(LocalDate.now())
                .effectiveDate(paidAt)
                .invoiceId(invoiceId)
                .status(FinancialEntry.Status.ACTIVE)
                .build();

        return processEntry(dto);
    }

    /**
     * Estornar lançamento
     */
    @Transactional
    public FinancialEntryDTO reverseEntry(Long entryId) {
        log.info("==========================================================");
        log.info(">>> ESTORNANDO LANÇAMENTO: ID={} <<<", entryId);
        log.info("==========================================================");

        Long companyId = CompanyContextHolder.getCompanyId();

        // Buscar lançamento original
        FinancialEntry originalEntry = financialEntryRepository
                .findByIdAndCompanyId(entryId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado: " + entryId));

        if (originalEntry.getStatus() != FinancialEntry.Status.ACTIVE) {
            throw new ValidationException("Lançamento já foi estornado ou cancelado");
        }

        // Marcar original como estornado
        originalEntry.setStatus(FinancialEntry.Status.REVERSED);
        financialEntryRepository.save(originalEntry);

        log.info("Lançamento original marcado como REVERSED");

        // Criar lançamento de estorno (inverso)
        FinancialEntry.EntryType reversalType = originalEntry.getEntryType() == FinancialEntry.EntryType.CREDIT
                ? FinancialEntry.EntryType.DEBIT
                : FinancialEntry.EntryType.CREDIT;

        FinancialEntryDTO reversalDTO = FinancialEntryDTO.builder()
                .companyId(companyId)
                .bankAccountId(originalEntry.getBankAccountId())
                .chartOfAccountId(originalEntry.getChartOfAccountId())
                .entryType(reversalType)
                .transactionType(FinancialEntry.TransactionType.ADJUSTMENT)
                .amount(originalEntry.getAmount())
                .description("ESTORNO: " + originalEntry.getDescription())
                .referenceDate(LocalDate.now())
                .effectiveDate(LocalDateTime.now())
                .reversedFromId(originalEntry.getId())
                .status(FinancialEntry.Status.ACTIVE)
                .notes("Estorno do lançamento #" + originalEntry.getId())
                .build();

        return processEntry(reversalDTO);
    }

    /**
     * Buscar lançamento por ID
     */
    @Transactional(readOnly = true)
    public FinancialEntryDTO findById(Long id) {
        Long companyId = CompanyContextHolder.getCompanyId();
        FinancialEntry entry = financialEntryRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado: " + id));
        return FinancialEntryDTO.fromEntity(entry);
    }

    /**
     * Listar lançamentos (paginado)
     */
    @Transactional(readOnly = true)
    public Page<FinancialEntryDTO> findAll(Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return financialEntryRepository.findByCompanyId(companyId, pageable)
                .map(FinancialEntryDTO::fromEntity);
    }

    /**
     * Listar lançamentos por conta bancária
     */
    @Transactional(readOnly = true)
    public Page<FinancialEntryDTO> findByBankAccount(Long bankAccountId, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return financialEntryRepository.findByCompanyIdAndBankAccountId(companyId, bankAccountId, pageable)
                .map(FinancialEntryDTO::fromEntity);
    }

    /**
     * Listar lançamentos por período
     */
    @Transactional(readOnly = true)
    public Page<FinancialEntryDTO> findByPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return financialEntryRepository.findByPeriod(companyId, startDate, endDate, pageable)
                .map(FinancialEntryDTO::fromEntity);
    }

    /**
     * Calcular total de entradas/saídas por período
     */
    @Transactional(readOnly = true)
    public BigDecimal sumByPeriodAndType(LocalDate startDate, LocalDate endDate,
                                         FinancialEntry.EntryType entryType) {
        Long companyId = CompanyContextHolder.getCompanyId();
        BigDecimal sum = financialEntryRepository.sumByPeriodAndType(companyId, startDate, endDate, entryType);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}

