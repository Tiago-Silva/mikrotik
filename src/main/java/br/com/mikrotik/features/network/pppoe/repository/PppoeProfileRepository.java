package br.com.mikrotik.features.network.pppoe.repository;

import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PppoeProfileRepository extends JpaRepository<PppoeProfile, Long> {
    Optional<PppoeProfile> findByNameAndMikrotikServer(String name, MikrotikServer server);
    List<PppoeProfile> findByMikrotikServer(MikrotikServer server);
    Page<PppoeProfile> findByMikrotikServer(MikrotikServer server, Pageable pageable);
    List<PppoeProfile> findByActiveAndMikrotikServer(Boolean active, MikrotikServer server);
}
