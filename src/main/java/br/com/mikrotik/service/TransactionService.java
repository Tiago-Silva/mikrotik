package br.com.mikrotik.service;

import br.com.mikrotik.dto.TransactionDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.Contract;
import br.com.mikrotik.model.Invoice;
import br.com.mikrotik.model.Transaction;
import br.com.mikrotik.repository.ContractRepository;
import br.com.mikrotik.repository.InvoiceRepository;
import br.com.mikrotik.repository.TransactionRepository;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;

    /**
     * Criar nova transação (registrar pagamento)
     */
    @Transactional
    public TransactionDTO create(TransactionDTO dto) {
        log.info("==========================================================");
        log.info(">>> REGISTRANDO PAGAMENTO - Fatura ID: {} <<<", dto.getInvoiceId());
        log.info("==========================================================");

        Long companyId = CompanyContextHolder.getCompanyId();

        // Validar se fatura existe e pertence à empresa
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(dto.getInvoiceId(), companyId)
                .orElseThrow(() -> new ValidationException("Fatura não encontrada ou não pertence à empresa"));

        log.info("Fatura encontrada - Contrato ID: {}, Status atual: {}", invoice.getContractId(), invoice.getStatus());

        // Verificar se fatura já está paga
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new ValidationException("Fatura já está marcada como paga");
        }

        // Salvar transação
        Transaction transaction = dto.toEntity();
        transaction = transactionRepository.save(transaction);
        log.info("Transação registrada: ID={}, Valor: {}, Método: {}",
                 transaction.getId(), transaction.getAmountPaid(), transaction.getMethod());

        // Atualizar status da fatura para PAID
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        log.info("✅ Status da fatura alterado para: PAID");

        // Verificar se deve reativar o contrato
        reactivateContractIfApplicable(invoice);

        log.info("==========================================================");
        log.info("✅ PAGAMENTO REGISTRADO COM SUCESSO - Transação ID: {}", transaction.getId());
        log.info("==========================================================");

        return TransactionDTO.fromEntity(transaction);
    }

    /**
     * Reativar contrato automaticamente após pagamento (se aplicável)
     */
    private void reactivateContractIfApplicable(Invoice paidInvoice) {
        try {
            log.info(">>> VERIFICANDO SE DEVE REATIVAR CONTRATO {} <<<", paidInvoice.getContractId());

            Long companyId = CompanyContextHolder.getCompanyId();

            // Buscar contrato
            Contract contract = contractRepository.findByIdAndCompanyId(paidInvoice.getContractId(), companyId)
                    .orElse(null);

            if (contract == null) {
                log.warn("Contrato não encontrado: {}", paidInvoice.getContractId());
                return;
            }

            log.info("Status atual do contrato: {}", contract.getStatus());

            // Só reativar se contrato estiver suspenso por inadimplência ou solicitação
            if (contract.getStatus() != Contract.ContractStatus.SUSPENDED_FINANCIAL &&
                contract.getStatus() != Contract.ContractStatus.SUSPENDED_REQUEST) {
                log.info("Contrato não está suspenso. Não é necessário reativar.");
                return;
            }

            // Verificar se ainda existem outras faturas em atraso (OVERDUE ou PENDING vencidas)
            List<Invoice> overdueInvoices = invoiceRepository.findAll()
                    .stream()
                    .filter(inv -> inv.getContractId().equals(contract.getId()))
                    .filter(inv -> inv.getStatus() == Invoice.InvoiceStatus.OVERDUE)
                    .toList();

            if (!overdueInvoices.isEmpty()) {
                log.warn("⚠️ CONTRATO AINDA POSSUI {} FATURA(S) EM ATRASO. NÃO SERÁ REATIVADO.",
                         overdueInvoices.size());
                return;
            }

            // Reativar contrato (isso também desbloqueia no Mikrotik)
            log.info("==========================================================");
            log.info(">>> REATIVANDO CONTRATO AUTOMATICAMENTE APÓS PAGAMENTO <<<");
            log.info("==========================================================");

            contractService.activate(contract.getId());

            log.info("==========================================================");
            log.info("✅ CONTRATO {} REATIVADO E DESBLOQUEADO NO MIKROTIK", contract.getId());
            log.info("==========================================================");

        } catch (Exception e) {
            log.error("❌ Erro ao tentar reativar contrato {}: {}",
                     paidInvoice.getContractId(), e.getMessage(), e);
            // Não lança exceção para não impedir o registro do pagamento
        }
    }

    /**
     * Buscar transação por ID
     */
    @Transactional(readOnly = true)
    public TransactionDTO findById(Long id) {
        log.info("Buscando transação por ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        return TransactionDTO.fromEntity(transaction);
    }

    /**
     * Listar todas as transações (paginado)
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findAll(Pageable pageable) {
        log.info("Listando todas as transações");

        Page<Transaction> transactions = transactionRepository.findAll(pageable);
        return transactions.map(TransactionDTO::fromEntity);
    }

    /**
     * Listar transações por fatura (paginado)
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByInvoice(Long invoiceId, Pageable pageable) {
        log.info("Listando transações da fatura: {}", invoiceId);

        Page<Transaction> transactions = transactionRepository.findByInvoiceId(invoiceId, pageable);
        return transactions.map(TransactionDTO::fromEntity);
    }

    /**
     * Listar transações por método de pagamento (paginado)
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByMethod(Transaction.PaymentMethod method, Pageable pageable) {
        log.info("Listando transações por método: {}", method);

        Page<Transaction> transactions = transactionRepository.findByMethod(method, pageable);
        return transactions.map(TransactionDTO::fromEntity);
    }

    /**
     * Listar transações por período (paginado)
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByPeriod(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Listando transações do período: {} a {}", startDate, endDate);

        Page<Transaction> transactions = transactionRepository.findByPeriod(startDate, endDate, pageable);
        return transactions.map(TransactionDTO::fromEntity);
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByFilters(Long invoiceId, Transaction.PaymentMethod method,
                                              LocalDateTime startDate, LocalDateTime endDate,
                                              Pageable pageable) {
        log.info("Buscando transações com filtros");

        Page<Transaction> transactions = transactionRepository.findByFilters(
                invoiceId, method, startDate, endDate, pageable);
        return transactions.map(TransactionDTO::fromEntity);
    }

    /**
     * Atualizar transação
     */
    @Transactional
    public TransactionDTO update(Long id, TransactionDTO dto) {
        log.info("Atualizando transação: ID={}", id);

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        existing.setAmountPaid(dto.getAmountPaid());
        existing.setPaidAt(dto.getPaidAt());
        existing.setMethod(dto.getMethod());
        existing.setTransactionCode(dto.getTransactionCode());
        existing.setNotes(dto.getNotes());

        existing = transactionRepository.save(existing);

        log.info("Transação atualizada com sucesso: ID={}", id);
        return TransactionDTO.fromEntity(existing);
    }

    /**
     * Deletar transação
     */
    @Transactional
    public void delete(Long id) {
        log.info("==========================================================");
        log.info(">>> DELETANDO TRANSAÇÃO: ID={} <<<", id);
        log.info("==========================================================");

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        Long invoiceId = transaction.getInvoiceId();
        log.info("Transação vinculada à fatura: {}", invoiceId);

        // Buscar fatura para reverter status
        Long companyId = CompanyContextHolder.getCompanyId();
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(invoiceId, companyId)
                .orElse(null);

        // Deletar transação
        transactionRepository.delete(transaction);
        log.info("✅ Transação deletada do banco de dados");

        // Reverter status da fatura
        if (invoice != null) {
            log.info("Revertendo status da fatura {} de {} para OVERDUE", invoiceId, invoice.getStatus());
            invoice.setStatus(Invoice.InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
            log.info("✅ Status da fatura revertido para: OVERDUE");

            // Suspender contrato novamente se estava ativo
            suspendContractIfApplicable(invoice);
        }

        log.info("==========================================================");
        log.info("✅ TRANSAÇÃO DELETADA COM SUCESSO: ID={}", id);
        log.info("==========================================================");
    }

    /**
     * Suspender contrato se ele estiver ativo e a fatura estiver vencida
     */
    private void suspendContractIfApplicable(Invoice overdueInvoice) {
        try {
            log.info(">>> VERIFICANDO SE DEVE SUSPENDER CONTRATO {} <<<", overdueInvoice.getContractId());

            Long companyId = CompanyContextHolder.getCompanyId();

            // Buscar contrato
            Contract contract = contractRepository.findByIdAndCompanyId(overdueInvoice.getContractId(), companyId)
                    .orElse(null);

            if (contract == null) {
                log.warn("Contrato não encontrado: {}", overdueInvoice.getContractId());
                return;
            }

            log.info("Status atual do contrato: {}", contract.getStatus());

            // Só suspender se contrato estiver ativo
            if (contract.getStatus() != Contract.ContractStatus.ACTIVE) {
                log.info("Contrato não está ativo. Não será suspenso.");
                return;
            }

            // Suspender por inadimplência
            log.info("==========================================================");
            log.info(">>> SUSPENDENDO CONTRATO POR REVERSÃO DE PAGAMENTO <<<");
            log.info("==========================================================");

            contractService.suspendFinancial(contract.getId());

            log.info("==========================================================");
            log.info("✅ CONTRATO {} SUSPENSO E BLOQUEADO NO MIKROTIK", contract.getId());
            log.info("==========================================================");

        } catch (Exception e) {
            log.error("❌ Erro ao tentar suspender contrato {}: {}",
                    overdueInvoice.getContractId(), e.getMessage(), e);
            // Não lança exceção para não impedir a deleção da transação
        }
    }

    /**
     * Contar transações por método
     */
    @Transactional(readOnly = true)
    public long countByMethod(Transaction.PaymentMethod method) {
        return transactionRepository.countByMethod(method);
    }
}
