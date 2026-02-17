package br.com.mikrotik.features.financial.repository;

import br.com.mikrotik.features.financial.model.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // Buscar por empresa (paginado)
    Page<BankAccount> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por empresa (lista)
    List<BankAccount> findByCompanyId(Long companyId);

    // Buscar por ID e empresa
    Optional<BankAccount> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar contas ativas
    Page<BankAccount> findByCompanyIdAndActive(Long companyId, Boolean active, Pageable pageable);

    // Buscar por tipo de conta
    Page<BankAccount> findByCompanyIdAndAccountType(Long companyId, BankAccount.AccountType accountType, Pageable pageable);

    // LOCK PESSIMISTA para atualizar saldo de forma thread-safe
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ba FROM BankAccount ba WHERE ba.id = :id AND ba.companyId = :companyId")
    Optional<BankAccount> findByIdAndCompanyIdWithLock(@Param("id") Long id, @Param("companyId") Long companyId);

    // Contar contas por empresa
    long countByCompanyId(Long companyId);

    // Buscar conta padrão (caixa) da empresa
    @Query("SELECT ba FROM BankAccount ba WHERE ba.companyId = :companyId AND ba.accountType = 'CASH' AND ba.active = true ORDER BY ba.id ASC")
    Optional<BankAccount> findDefaultCashAccount(@Param("companyId") Long companyId);
}

