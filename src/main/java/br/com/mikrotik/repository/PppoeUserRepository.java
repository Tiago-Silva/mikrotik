package br.com.mikrotik.repository;

import br.com.mikrotik.model.PppoeUser;
import br.com.mikrotik.model.MikrotikServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PppoeUserRepository extends JpaRepository<PppoeUser, Long> {
    Optional<PppoeUser> findByUsername(String username);
    Optional<PppoeUser> findByUsernameAndMikrotikServer(String username, MikrotikServer server);
    Optional<PppoeUser> findByUsernameAndMikrotikServerId(String username, Long mikrotikServerId);

    List<PppoeUser> findByMikrotikServer(MikrotikServer server);
    Page<PppoeUser> findByMikrotikServer(MikrotikServer server, Pageable pageable);

    List<PppoeUser> findByActiveAndMikrotikServer(Boolean active, MikrotikServer server);

    // Filtros por company
    List<PppoeUser> findByCompanyId(Long companyId);
    Page<PppoeUser> findByCompanyId(Long companyId, Pageable pageable);

    // Filtros por status
    List<PppoeUser> findByStatus(PppoeUser.UserStatus status);
    Page<PppoeUser> findByCompanyIdAndStatus(Long companyId, PppoeUser.UserStatus status, Pageable pageable);

    // Contadores
    Long countByActiveTrue();
    Long countByCompanyId(Long companyId);
    Long countByCompanyIdAndStatus(Long companyId, PppoeUser.UserStatus status);
}
