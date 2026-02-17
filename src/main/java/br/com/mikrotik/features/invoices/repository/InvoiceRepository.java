package br.com.mikrotik.features.invoices.repository;

import br.com.mikrotik.features.invoices.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Buscar por empresa (multi-tenant)
    Page<Invoice> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por contrato
    Page<Invoice> findByContractId(Long contractId, Pageable pageable);

    // Buscar por cliente
    Page<Invoice> findByCustomerId(Long customerId, Pageable pageable);

    // Buscar por status
    Page<Invoice> findByCompanyIdAndStatus(Long companyId, Invoice.InvoiceStatus status, Pageable pageable);

    // Buscar por ID e empresa (segurança multi-tenant)
    Optional<Invoice> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar faturas vencidas (paginado)
    @Query("SELECT i FROM Invoice i WHERE i.companyId = :companyId " +
           "AND i.status IN ('PENDING', 'OVERDUE') AND i.dueDate < :today")
    Page<Invoice> findOverdueInvoices(@Param("companyId") Long companyId, @Param("today") LocalDate today, Pageable pageable);

    // Buscar por mês de referência
    List<Invoice> findByCompanyIdAndReferenceMonth(Long companyId, LocalDate referenceMonth);

    // Verificar se já existe fatura para contrato no mês
    boolean existsByContractIdAndReferenceMonth(Long contractId, LocalDate referenceMonth);

    // Contar faturas por empresa
    long countByCompanyId(Long companyId);

    // Contar faturas por status
    long countByCompanyIdAndStatus(Long companyId, Invoice.InvoiceStatus status);

    // Buscar com filtros múltiplos
    @Query("SELECT i FROM Invoice i WHERE i.companyId = :companyId " +
           "AND (:customerId IS NULL OR i.customerId = :customerId) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:month IS NULL OR i.referenceMonth = :month)")
    Page<Invoice> findByFilters(@Param("companyId") Long companyId,
                                @Param("customerId") Long customerId,
                                @Param("status") Invoice.InvoiceStatus status,
                                @Param("month") LocalDate month,
                                Pageable pageable);

    // Buscar contratos com faturas vencidas há X dias ou mais para suspensão automática
    @Query("SELECT DISTINCT i.contractId FROM Invoice i " +
           "WHERE i.companyId = :companyId " +
           "AND i.status = 'OVERDUE' " +
           "AND i.dueDate <= :suspensionDate")
    List<Long> findContractIdsForSuspension(@Param("companyId") Long companyId,
                                            @Param("suspensionDate") LocalDate suspensionDate);
}
