package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pppoe_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String email;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(name = "static_ip", length = 45)
    private String staticIp;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UserStatus status = UserStatus.OFFLINE;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "last_connection_at")
    private LocalDateTime lastConnectionAt;

    @ManyToOne
    @JoinColumn(name = "pppoe_profile_id", nullable = false)
    private PppoeProfile profile;

    @ManyToOne
    @JoinColumn(name = "mikrotik_server_id", nullable = false)
    private MikrotikServer mikrotikServer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum UserStatus {
        ONLINE,
        OFFLINE,
        DISABLED
    }
}
