package br.com.mikrotik.service;

import br.com.mikrotik.dto.TransactionDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.Invoice;
import br.com.mikrotik.model.Transaction;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;

    /**
     * Criar nova transação (registrar pagamento)
     */
    @Transactional
    public TransactionDTO create(TransactionDTO dto) {
        log.info("Criando nova transação para fatura: {}", dto.getInvoiceId());

        Long companyId = CompanyContextHolder.getCompanyId();

        // Validar se fatura existe e pertence à empresa
        Invoice invoice = invoiceRepository.findByIdAndCompanyId(dto.getInvoiceId(), companyId)
                .orElseThrow(() -> new ValidationException("Fatura não encontrada ou não pertence à empresa"));

        // Verificar se fatura já está paga
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new ValidationException("Fatura já está marcada como paga");
        }

        Transaction transaction = dto.toEntity();
        transaction = transactionRepository.save(transaction);

        // Atualizar status da fatura para PAID
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        log.info("Transação criada com sucesso: ID={}", transaction.getId());
        return TransactionDTO.fromEntity(transaction);
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
     * Listar transações por fatura
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> findByInvoice(Long invoiceId) {
        log.info("Listando transações da fatura: {}", invoiceId);

        List<Transaction> transactions = transactionRepository.findByInvoiceId(invoiceId);
        return transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
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
     * Listar transações por período
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> findByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Listando transações do período: {} a {}", startDate, endDate);

        List<Transaction> transactions = transactionRepository.findByPeriod(startDate, endDate);
        return transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
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
        log.info("Deletando transação: ID={}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        transactionRepository.delete(transaction);
        log.info("Transação deletada com sucesso: ID={}", id);
    }

    /**
     * Contar transações por método
     */
    @Transactional(readOnly = true)
    public long countByMethod(Transaction.PaymentMethod method) {
        return transactionRepository.countByMethod(method);
    }
}
