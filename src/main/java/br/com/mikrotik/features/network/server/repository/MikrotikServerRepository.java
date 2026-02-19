package br.com.mikrotik.features.network.server.repository;

import br.com.mikrotik.features.network.server.model.MikrotikServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MikrotikServerRepository extends JpaRepository<MikrotikServer, Long> {

    // Métodos antigos - mantidos para backward compatibility
    @Deprecated(since = "2.0", forRemoval = false)
    Optional<MikrotikServer> findByName(String name);

    Optional<MikrotikServer> findByIpAddress(String ipAddress);

    Long countByActiveTrue();

    // Novos métodos com suporte multi-tenant
    Page<MikrotikServer> findByCompanyId(Long companyId, Pageable pageable);

    Page<MikrotikServer> findByCompanyIdAndActive(Long companyId, Boolean active, Pageable pageable);

    List<MikrotikServer> findByCompanyIdAndActiveTrue(Long companyId);

    Optional<MikrotikServer> findByNameAndCompanyId(String name, Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);

    Long countByCompanyId(Long companyId);

    Long countByCompanyIdAndActiveTrue(Long companyId);
}
