package br.com.mikrotik.repository;

import br.com.mikrotik.model.PppoeProfile;
import br.com.mikrotik.model.MikrotikServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PppoeProfileRepository extends JpaRepository<PppoeProfile, Long> {
    Optional<PppoeProfile> findByNameAndMikrotikServer(String name, MikrotikServer server);
    List<PppoeProfile> findByMikrotikServer(MikrotikServer server);
    List<PppoeProfile> findByActiveAndMikrotikServer(Boolean active, MikrotikServer server);
}
