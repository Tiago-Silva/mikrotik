package br.com.mikrotik.repository;

import br.com.mikrotik.model.Contract;
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
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Buscar por empresa (multi-tenant)
    Page<Contract> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por cliente
    Page<Contract> findByCustomerId(Long customerId, Pageable pageable);

    // Buscar por status
    Page<Contract> findByCompanyIdAndStatus(Long companyId, Contract.ContractStatus status, Pageable pageable);

    // Buscar por ID e empresa (segurança multi-tenant)
    Optional<Contract> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar contratos ativos
    List<Contract> findByCompanyIdAndStatus(Long companyId, Contract.ContractStatus status);

    // Buscar por usuário PPPoE
    Optional<Contract> findByPppoeUserId(Long pppoeUserId);

    // Buscar por usuário PPPoE e empresa
    Optional<Contract> findByPppoeUserIdAndCompanyId(Long pppoeUserId, Long companyId);

    // Buscar contratos ativos por plano de serviço
    List<Contract> findByServicePlanIdAndStatus(Long servicePlanId, Contract.ContractStatus status);

    // Verificar se cliente possui contratos ativos
    boolean existsByCustomerIdAndStatus(Long customerId, Contract.ContractStatus status);

    // Contar contratos por empresa
    long countByCompanyId(Long companyId);

    // Contar contratos por status
    long countByCompanyIdAndStatus(Long companyId, Contract.ContractStatus status);

    // Buscar contratos com vencimento próximo (para renovação)
    @Query("SELECT c FROM Contract c WHERE c.companyId = :companyId " +
           "AND c.endDate IS NOT NULL AND c.endDate BETWEEN :startDate AND :endDate")
    List<Contract> findExpiringContracts(@Param("companyId") Long companyId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // Buscar contratos para faturamento (dia de cobrança) - paginado
    @Query("SELECT c FROM Contract c WHERE c.companyId = :companyId " +
           "AND c.status = 'ACTIVE' AND c.billingDay = :day")
    Page<Contract> findContractsForBilling(@Param("companyId") Long companyId,
                                           @Param("day") Integer day,
                                           Pageable pageable);

    // Buscar com filtros múltiplos
    @Query("SELECT c FROM Contract c WHERE c.companyId = :companyId " +
           "AND (:customerId IS NULL OR c.customerId = :customerId) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:servicePlanId IS NULL OR c.servicePlanId = :servicePlanId)")
    Page<Contract> findByFilters(@Param("companyId") Long companyId,
                                 @Param("customerId") Long customerId,
                                 @Param("status") Contract.ContractStatus status,
                                 @Param("servicePlanId") Long servicePlanId,
                                 Pageable pageable);
}
