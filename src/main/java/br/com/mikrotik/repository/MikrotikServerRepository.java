package br.com.mikrotik.repository;

import br.com.mikrotik.model.MikrotikServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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

    Optional<MikrotikServer> findByNameAndCompanyId(String name, Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);

    Long countByCompanyIdAndActiveTrue(Long companyId);
}
