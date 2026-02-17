package br.com.mikrotik.features.network.server.model;

import br.com.mikrotik.features.companies.model.Company;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mikrotik_servers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MikrotikServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column(name = "company_id")
    private Long companyId;

    // Multi-tenant support - NULLABLE for backward compatibility
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "ip_address")
    private String ipAddress;

    @Column(nullable = false, name = "ssh_port")
    private Integer port = 22; // Porta SSH padrão

    @Column(name = "api_port")
    private Integer apiPort = 8728; // Porta API padrão do Mikrotik

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime lastSyncAt;

    @Enumerated(EnumType.STRING)
    private SyncStatus syncStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum SyncStatus {
        OK, ERROR, UNREACHABLE
    }
}
