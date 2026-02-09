package br.com.mikrotik.repository;

import br.com.mikrotik.model.ChartOfAccounts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountsRepository extends JpaRepository<ChartOfAccounts, Long> {

    // Buscar por empresa (paginado)
    Page<ChartOfAccounts> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por empresa (lista)
    List<ChartOfAccounts> findByCompanyId(Long companyId);

    // Buscar por ID e empresa
    Optional<ChartOfAccounts> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar por código e empresa
    Optional<ChartOfAccounts> findByCodeAndCompanyId(String code, Long companyId);

    // Buscar por tipo de conta
    Page<ChartOfAccounts> findByCompanyIdAndAccountType(Long companyId, ChartOfAccounts.AccountType accountType, Pageable pageable);

    // Buscar por categoria
    Page<ChartOfAccounts> findByCompanyIdAndCategory(Long companyId, ChartOfAccounts.Category category, Pageable pageable);

    // Buscar contas ativas
    List<ChartOfAccounts> findByCompanyIdAndActive(Long companyId, Boolean active);

    // Buscar contas pai (sem parent)
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.companyId = :companyId AND c.parentId IS NULL")
    List<ChartOfAccounts> findParentAccounts(@Param("companyId") Long companyId);

    // Buscar contas filhas
    List<ChartOfAccounts> findByCompanyIdAndParentId(Long companyId, Long parentId);

    // Buscar conta de receita de assinatura (padrão para faturas)
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.companyId = :companyId AND c.category = 'SUBSCRIPTION_REVENUE' AND c.active = true ORDER BY c.id ASC")
    Optional<ChartOfAccounts> findDefaultSubscriptionRevenueAccount(@Param("companyId") Long companyId);

    // Contar contas por empresa
    long countByCompanyId(Long companyId);
}

