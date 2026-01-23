package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "internet_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternetProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "mikrotik_server_id", nullable = false)
    private Long mikrotikServerId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "download_kbit", nullable = false)
    private Long downloadKbit;

    @Column(name = "upload_kbit", nullable = false)
    private Long uploadKbit;

    @Column(name = "session_timeout", nullable = false)
    @Builder.Default
    private Integer sessionTimeout = 0;

    @Column(name = "remote_address_pool_id")
    private Long remoteAddressPoolId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remote_address_pool_id", insertable = false, updatable = false)
    private IpPool remoteAddressPool;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (sessionTimeout == null) {
            sessionTimeout = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
