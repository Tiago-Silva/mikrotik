package br.com.mikrotik.repository;

import br.com.mikrotik.model.MikrotikServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MikrotikServerRepository extends JpaRepository<MikrotikServer, Long> {
    Optional<MikrotikServer> findByName(String name);
    Optional<MikrotikServer> findByIpAddress(String ipAddress);
}
