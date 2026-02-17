package br.com.mikrotik.features.dashboard.service;

import br.com.mikrotik.features.network.server.dto.ConnectionStatusDTO;
import br.com.mikrotik.features.dashboard.dto.DashboardStatsDTO;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeConnectionRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeProfileRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final MikrotikServerRepository mikrotikServerRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final PppoeProfileRepository pppoeProfileRepository;
    private final PppoeConnectionRepository pppoeConnectionRepository;

    public DashboardStatsDTO getStats() {
        log.info("Fetching dashboard statistics");

        Long totalServers = mikrotikServerRepository.count();
        Long activeServers = mikrotikServerRepository.countByActiveTrue();

        Long totalUsers = pppoeUserRepository.count();
        Long activeUsers = pppoeUserRepository.countByActiveTrue();

        Long totalProfiles = pppoeProfileRepository.count();

        Long activeConnections = pppoeConnectionRepository.countByActiveTrue();
        Long totalConnections = pppoeConnectionRepository.count();

        return DashboardStatsDTO.builder()
                .totalServers(totalServers)
                .activeServers(activeServers)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalProfiles(totalProfiles)
                .activeConnections(activeConnections)
                .totalConnections(totalConnections)
                .build();
    }

    public ConnectionStatusDTO getConnectionStatus() {
        log.info("Fetching connection status");

        Long activeConnections = pppoeConnectionRepository.countByActiveTrue();
        Long totalUsers = pppoeUserRepository.countByActiveTrue();

        // Considera que a capacidade total é o número de usuários ativos
        Long totalCapacity = totalUsers > 0 ? totalUsers : 1L;

        Double utilizationPercentage = (activeConnections.doubleValue() / totalCapacity.doubleValue()) * 100;

        String status;
        String message;

        if (utilizationPercentage >= 90) {
            status = "degraded";
            message = "System is near capacity";
        } else if (utilizationPercentage >= 70) {
            status = "online";
            message = "System is operating normally with high load";
        } else {
            status = "online";
            message = "System is operating normally";
        }

        return ConnectionStatusDTO.builder()
                .status(status)
                .activeConnections(activeConnections)
                .totalCapacity(totalCapacity)
                .utilizationPercentage(Math.round(utilizationPercentage * 100.0) / 100.0)
                .message(message)
                .build();
    }
}
