package br.com.mikrotik.repository;

import br.com.mikrotik.model.ServicePlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePlanRepository extends JpaRepository<ServicePlan, Long> {

    // Buscar por empresa (multi-tenant)
    Page<ServicePlan> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar ativos por empresa
    Page<ServicePlan> findByCompanyIdAndActive(Long companyId, Boolean active, Pageable pageable);

    // Listar todos por empresa (sem paginação)
    List<ServicePlan> findByCompanyId(Long companyId);

    // Listar ativos por empresa (sem paginação)
    List<ServicePlan> findByCompanyIdAndActive(Long companyId, Boolean active);

    // Buscar por nome e empresa
    Optional<ServicePlan> findByNameAndCompanyId(String name, Long companyId);

    // Buscar por ID e empresa (segurança multi-tenant)
    Optional<ServicePlan> findByIdAndCompanyId(Long id, Long companyId);

    // Verificar existência por nome e empresa
    boolean existsByNameAndCompanyId(String name, Long companyId);

    // Buscar planos que usam um internet profile específico
    List<ServicePlan> findByInternetProfileId(Long internetProfileId);

    // Contar planos por empresa
    long countByCompanyId(Long companyId);

    // Contar planos ativos por empresa
    long countByCompanyIdAndActive(Long companyId, Boolean active);

    // Buscar com filtros múltiplos
    @Query("SELECT sp FROM ServicePlan sp WHERE sp.companyId = :companyId " +
           "AND (:name IS NULL OR LOWER(sp.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:active IS NULL OR sp.active = :active)")
    Page<ServicePlan> findByFilters(@Param("companyId") Long companyId,
                                    @Param("name") String name,
                                    @Param("active") Boolean active,
                                    Pageable pageable);
}
