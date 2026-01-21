package br.com.mikrotik.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "pppoe_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pppoe_user_id", nullable = false)
    private PppoeUser user;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String callingStationId; // MAC Address

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    private LocalDateTime disconnectedAt;

    @Column(nullable = false)
    private Long bytesUp = 0L;

    @Column(nullable = false)
    private Long bytesDown = 0L;

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
