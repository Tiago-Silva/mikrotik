package br.com.mikrotik.repository;

import br.com.mikrotik.model.AutomationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AutomationLogRepository extends JpaRepository<AutomationLog, Long> {

    // Buscar por empresa (multi-tenant)
    Page<AutomationLog> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por contrato
    Page<AutomationLog> findByContractId(Long contractId, Pageable pageable);

    // Buscar por tipo de ação
    Page<AutomationLog> findByCompanyIdAndActionType(Long companyId, AutomationLog.ActionType actionType, Pageable pageable);

    // Buscar por sucesso/falha
    Page<AutomationLog> findByCompanyIdAndSuccess(Long companyId, Boolean success, Pageable pageable);

    // Buscar por período
    @Query("SELECT al FROM AutomationLog al WHERE al.companyId = :companyId " +
           "AND al.executedAt BETWEEN :startDate AND :endDate")
    List<AutomationLog> findByPeriod(@Param("companyId") Long companyId,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    // Buscar logs recentes
    List<AutomationLog> findTop100ByCompanyIdOrderByExecutedAtDesc(Long companyId);

    // Contar logs por empresa
    long countByCompanyId(Long companyId);

    // Contar logs por tipo de ação
    long countByCompanyIdAndActionType(Long companyId, AutomationLog.ActionType actionType);

    // Contar logs com sucesso
    long countByCompanyIdAndSuccess(Long companyId, Boolean success);

    // Buscar com filtros múltiplos
    @Query("SELECT al FROM AutomationLog al WHERE al.companyId = :companyId " +
           "AND (:contractId IS NULL OR al.contractId = :contractId) " +
           "AND (:actionType IS NULL OR al.actionType = :actionType) " +
           "AND (:success IS NULL OR al.success = :success)")
    Page<AutomationLog> findByFilters(@Param("companyId") Long companyId,
                                      @Param("contractId") Long contractId,
                                      @Param("actionType") AutomationLog.ActionType actionType,
                                      @Param("success") Boolean success,
                                      Pageable pageable);
}
