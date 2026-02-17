package br.com.mikrotik.features.network.pppoe.repository;

import br.com.mikrotik.features.network.pppoe.model.PppoeConnection;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PppoeConnectionRepository extends JpaRepository<PppoeConnection, Long> {
    List<PppoeConnection> findByUserAndActive(PppoeUser user, Boolean active);
    Page<PppoeConnection> findByMikrotikServer(MikrotikServer server, Pageable pageable);
    List<PppoeConnection> findByMikrotikServerAndActive(MikrotikServer server, Boolean active);
    Page<PppoeConnection> findByMikrotikServerAndActive(MikrotikServer server, Boolean active, Pageable pageable);
    List<PppoeConnection> findByUser(PppoeUser user);
    Page<PppoeConnection> findByUser(PppoeUser user, Pageable pageable);
    Long countByActiveTrue();
}
