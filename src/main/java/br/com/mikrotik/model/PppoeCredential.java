package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pppoe_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PppoeCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "mikrotik_server_id", nullable = false)
    private Long mikrotikServerId;

    @Column(length = 100, nullable = false)
    private String username;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(name = "static_ip", length = 45)
    private String staticIp;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private CredentialStatus status = CredentialStatus.OFFLINE;

    @Column(name = "last_connection_at")
    private LocalDateTime lastConnectionAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mikrotik_server_id", insertable = false, updatable = false)
    private MikrotikServer mikrotikServer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CredentialStatus.OFFLINE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CredentialStatus {
        ONLINE,
        OFFLINE,
        DISABLED
    }
}
