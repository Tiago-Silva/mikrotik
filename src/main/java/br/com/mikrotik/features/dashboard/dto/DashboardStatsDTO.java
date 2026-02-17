package br.com.mikrotik.features.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Long totalServers;
    private Long activeServers;
    private Long totalUsers;
    private Long activeUsers;
    private Long totalProfiles;
    private Long activeConnections;
    private Long totalConnections;
}
