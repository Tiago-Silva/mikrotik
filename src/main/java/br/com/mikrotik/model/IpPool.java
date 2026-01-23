package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ip_pools")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mikrotik_server_id", nullable = false)
    private Long mikrotikServerId;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 20)
    private String cidr;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mikrotik_server_id", insertable = false, updatable = false)
    private MikrotikServer mikrotikServer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }
}
