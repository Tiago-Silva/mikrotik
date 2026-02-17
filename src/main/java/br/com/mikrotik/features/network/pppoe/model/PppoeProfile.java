package br.com.mikrotik.features.network.pppoe.model;

import br.com.mikrotik.features.network.server.model.MikrotikServer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pppoe_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Long maxBitrateDl; // Download em bps

    @Column(nullable = false)
    private Long maxBitrateUl; // Upload em bps

    @Column(nullable = false)
    private Integer sessionTimeout; // em segundos

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "mikrotik_server_id", nullable = false)
    private MikrotikServer mikrotikServer;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
