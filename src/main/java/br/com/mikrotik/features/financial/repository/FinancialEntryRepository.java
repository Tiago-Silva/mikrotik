package br.com.mikrotik.features.financial.repository;

import br.com.mikrotik.features.financial.model.FinancialEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, Long> {

    // Buscar por empresa (paginado)
    Page<FinancialEntry> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por ID e empresa
    Optional<FinancialEntry> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar por conta bancária
    Page<FinancialEntry> findByCompanyIdAndBankAccountId(Long companyId, Long bankAccountId, Pageable pageable);

    // Buscar por conta do plano de contas
    Page<FinancialEntry> findByCompanyIdAndChartOfAccountId(Long companyId, Long chartOfAccountId, Pageable pageable);

    // Buscar por período (data de competência)
    @Query("SELECT fe FROM FinancialEntry fe WHERE fe.companyId = :companyId AND fe.referenceDate BETWEEN :startDate AND :endDate AND fe.status = 'ACTIVE'")
    Page<FinancialEntry> findByPeriod(@Param("companyId") Long companyId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);

    // Buscar por tipo de entrada
    Page<FinancialEntry> findByCompanyIdAndEntryTypeAndStatus(Long companyId, FinancialEntry.EntryType entryType, FinancialEntry.Status status, Pageable pageable);

    // Buscar por fatura
    List<FinancialEntry> findByInvoiceIdAndStatus(Long invoiceId, FinancialEntry.Status status);

    // Soma de entradas por período e tipo (para relatórios)
    @Query("SELECT SUM(fe.amount) FROM FinancialEntry fe WHERE fe.companyId = :companyId " +
           "AND fe.referenceDate BETWEEN :startDate AND :endDate " +
           "AND fe.entryType = :entryType AND fe.status = 'ACTIVE'")
    BigDecimal sumByPeriodAndType(@Param("companyId") Long companyId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate,
                                   @Param("entryType") FinancialEntry.EntryType entryType);

    // Soma de entradas por conta bancária e período
    @Query("SELECT SUM(CASE WHEN fe.entryType = 'CREDIT' THEN fe.amount ELSE -fe.amount END) " +
           "FROM FinancialEntry fe WHERE fe.bankAccountId = :bankAccountId " +
           "AND fe.effectiveDate BETWEEN :startDate AND :endDate AND fe.status = 'ACTIVE'")
    BigDecimal sumBalanceByPeriod(@Param("bankAccountId") Long bankAccountId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // Buscar lançamentos do dia para uma conta (para consolidação)
    @Query("SELECT fe FROM FinancialEntry fe WHERE fe.bankAccountId = :bankAccountId " +
           "AND DATE(fe.effectiveDate) = :date AND fe.status = 'ACTIVE'")
    List<FinancialEntry> findByBankAccountAndDate(@Param("bankAccountId") Long bankAccountId,
                                                   @Param("date") LocalDate date);

    // Contar lançamentos por empresa
    long countByCompanyId(Long companyId);
}

