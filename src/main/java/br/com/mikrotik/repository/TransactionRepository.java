package br.com.mikrotik.repository;

import br.com.mikrotik.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Buscar por fatura
    List<Transaction> findByInvoiceId(Long invoiceId);

    // Buscar por fatura (paginado)
    Page<Transaction> findByInvoiceId(Long invoiceId, Pageable pageable);

    // Buscar por método de pagamento
    Page<Transaction> findByMethod(Transaction.PaymentMethod method, Pageable pageable);

    // Buscar por período
    @Query("SELECT t FROM Transaction t WHERE t.paidAt BETWEEN :startDate AND :endDate")
    List<Transaction> findByPeriod(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Buscar por código de transação
    List<Transaction> findByTransactionCode(String transactionCode);

    // Buscar com filtros múltiplos
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:invoiceId IS NULL OR t.invoiceId = :invoiceId) " +
           "AND (:method IS NULL OR t.method = :method) " +
           "AND (:startDate IS NULL OR t.paidAt >= :startDate) " +
           "AND (:endDate IS NULL OR t.paidAt <= :endDate)")
    Page<Transaction> findByFilters(@Param("invoiceId") Long invoiceId,
                                    @Param("method") Transaction.PaymentMethod method,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);

    // Contar transações por método
    long countByMethod(Transaction.PaymentMethod method);
}
