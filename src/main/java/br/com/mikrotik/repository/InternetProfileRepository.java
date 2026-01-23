package br.com.mikrotik.repository;

import br.com.mikrotik.model.InternetProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InternetProfileRepository extends JpaRepository<InternetProfile, Long> {

    // Buscar por empresa (multi-tenant)
    Page<InternetProfile> findByCompanyId(Long companyId, Pageable pageable);

    // Buscar por empresa e servidor
    Page<InternetProfile> findByCompanyIdAndMikrotikServerId(Long companyId, Long mikrotikServerId, Pageable pageable);

    // Buscar ativos por empresa
    Page<InternetProfile> findByCompanyIdAndActive(Long companyId, Boolean active, Pageable pageable);

    // Buscar ativos por empresa e servidor
    Page<InternetProfile> findByCompanyIdAndMikrotikServerIdAndActive(
            Long companyId, Long mikrotikServerId, Boolean active, Pageable pageable);

    // Listar todos por empresa (sem paginação)
    List<InternetProfile> findByCompanyId(Long companyId);

    // Listar ativos por empresa (sem paginação)
    List<InternetProfile> findByCompanyIdAndActive(Long companyId, Boolean active);

    // Buscar por nome e empresa
    Optional<InternetProfile> findByNameAndCompanyId(String name, Long companyId);

    // Buscar por ID e empresa (segurança multi-tenant)
    Optional<InternetProfile> findByIdAndCompanyId(Long id, Long companyId);

    // Verificar existência por nome e empresa
    boolean existsByNameAndCompanyId(String name, Long companyId);

    // Contar perfis por empresa
    long countByCompanyId(Long companyId);

    // Contar perfis ativos por empresa
    long countByCompanyIdAndActive(Long companyId, Boolean active);

    // Contar perfis por servidor
    long countByMikrotikServerId(Long mikrotikServerId);

    // Buscar perfis que usam um pool específico
    @Query("SELECT ip FROM InternetProfile ip WHERE ip.remoteAddressPoolId = :poolId")
    List<InternetProfile> findByRemoteAddressPoolId(@Param("poolId") Long poolId);

    // Buscar com filtros múltiplos
    @Query("SELECT ip FROM InternetProfile ip WHERE ip.companyId = :companyId " +
           "AND (:name IS NULL OR LOWER(ip.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:serverId IS NULL OR ip.mikrotikServerId = :serverId) " +
           "AND (:active IS NULL OR ip.active = :active)")
    Page<InternetProfile> findByFilters(@Param("companyId") Long companyId,
                                        @Param("name") String name,
                                        @Param("serverId") Long serverId,
                                        @Param("active") Boolean active,
                                        Pageable pageable);
}
