package br.com.mikrotik.features.financial.repository;

import br.com.mikrotik.features.financial.model.DailyBalance;
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
public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {

    // Buscar por empresa
    Page<DailyBalance> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por conta bancária
    Page<DailyBalance> findByBankAccountId(Long bankAccountId, Pageable pageable);

    // Buscar por conta bancária e data
    Optional<DailyBalance> findByBankAccountIdAndBalanceDate(Long bankAccountId, LocalDate balanceDate);

    // Buscar por conta bancária e período
    @Query("SELECT db FROM DailyBalance db WHERE db.bankAccountId = :bankAccountId " +
           "AND db.balanceDate BETWEEN :startDate AND :endDate ORDER BY db.balanceDate ASC")
    List<DailyBalance> findByBankAccountAndPeriod(@Param("bankAccountId") Long bankAccountId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Buscar último saldo consolidado de uma conta
    @Query("SELECT db FROM DailyBalance db WHERE db.bankAccountId = :bankAccountId " +
           "ORDER BY db.balanceDate DESC LIMIT 1")
    Optional<DailyBalance> findLastBalanceByAccount(@Param("bankAccountId") Long bankAccountId);

    // Buscar saldos de todas as contas de uma empresa em uma data específica
    @Query("SELECT db FROM DailyBalance db WHERE db.companyId = :companyId AND db.balanceDate = :date")
    List<DailyBalance> findByCompanyAndDate(@Param("companyId") Long companyId, @Param("date") LocalDate date);

    // Verificar se já existe consolidação para uma data
    boolean existsByBankAccountIdAndBalanceDate(Long bankAccountId, LocalDate balanceDate);
}

