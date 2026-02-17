package br.com.mikrotik.features.network.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionStatusDTO {
    private String status; // "online", "offline", "degraded"
    private Long activeConnections;
    private Long totalCapacity;
    private Double utilizationPercentage;
    private String message;
}
