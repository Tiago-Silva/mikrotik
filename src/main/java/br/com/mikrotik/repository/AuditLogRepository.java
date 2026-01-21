package br.com.mikrotik.repository;

import br.com.mikrotik.model.AuditLog;
import br.com.mikrotik.model.MikrotikServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByMikrotikServer(MikrotikServer server, Pageable pageable);
    Page<AuditLog> findByEntity(String entity, Pageable pageable);
}
