package br.com.mikrotik.features.dashboard.service;

import br.com.mikrotik.features.network.server.dto.ConnectionStatusDTO;
import br.com.mikrotik.features.dashboard.dto.DashboardStatsDTO;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeProfileRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Serviço de Dashboard com dados em tempo real do Mikrotik
 *
 * ARQUITETURA:
 * - Estatísticas de cadastros vêm do banco (rápido, ACID)
 * - Conexões ativas vêm do Mikrotik via API (tempo real, eventual consistency)
 * - Não realizamos chamadas externas dentro de transações
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final MikrotikServerRepository mikrotikServerRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final PppoeProfileRepository pppoeProfileRepository;
    private final MikrotikApiService mikrotikApiService;

    /**
     * Retorna estatísticas do dashboard com dados em tempo real do Mikrotik
     *
     * IMPORTANTE: Quebra de transação aqui.
     * 1. Consulta dados do banco (dentro da transação read-only)
     * 2. Consulta Mikrotik fora da transação (não bloqueia pool de conexões)
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        log.info("Fetching dashboard statistics");

        Long companyId = CompanyContextHolder.getCompanyId();

        // === FASE 1: Dados do Banco (rápido, transacional) ===
        Long totalServers = mikrotikServerRepository.countByCompanyId(companyId);
        Long activeServers = mikrotikServerRepository.countByCompanyIdAndActiveTrue(companyId);

        Long totalUsers = pppoeUserRepository.count();
        Long activeUsers = pppoeUserRepository.countByActiveTrue();

        Long totalProfiles = pppoeProfileRepository.count();

        // === FASE 2: Dados em Tempo Real do Mikrotik (fora da transação) ===
        ConnectionStats connectionStats = fetchRealTimeConnectionStats(companyId);

        return DashboardStatsDTO.builder()
                .totalServers(totalServers)
                .activeServers(activeServers)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalProfiles(totalProfiles)
                .onlineConnections(connectionStats.online)
                .offlineConnections(connectionStats.offline)
                .pendingConnections(connectionStats.pending)
                .totalConnections(connectionStats.total())
                .build();
    }

    /**
     * Consulta dados de conexão em tempo real de todos os servidores Mikrotik ativos
     *
     * ATENÇÃO: Método executado FORA de transação para não bloquear o pool de conexões do banco
     * enquanto aguarda resposta dos servidores Mikrotik.
     */
    private ConnectionStats fetchRealTimeConnectionStats(Long companyId) {
        try {
            // Busca servidores ativos (já estamos fora da transação principal aqui)
            List<MikrotikServer> activeServers = mikrotikServerRepository.findByCompanyIdAndActiveTrue(companyId);

            if (activeServers.isEmpty()) {
                log.warn("Nenhum servidor Mikrotik ativo encontrado para company {}", companyId);
                return new ConnectionStats(0L, 0L, 0L);
            }

            long totalOnline = 0;

            // Itera sobre cada servidor e soma conexões ativas
            for (MikrotikServer server : activeServers) {
                try {
                    log.debug("Consultando conexões ativas do servidor: {}", server.getName());

                    List<Map<String, String>> activeConnections = mikrotikApiService.listActivePppoeConnections(
                            server.getIpAddress(),
                            server.getApiPort(),
                            server.getUsername(),
                            server.getPassword()
                    );

                    totalOnline += activeConnections.size();

                    log.debug("Servidor {} tem {} conexões ativas", server.getName(), activeConnections.size());

                } catch (Exception e) {
                    log.error("Erro ao consultar servidor {} ({}): {}",
                            server.getName(), server.getIpAddress(), e.getMessage());
                    // Continua para o próximo servidor (resiliência)
                }
            }

            // Calcula offline e pending baseado nos usuários cadastrados
            Long totalActiveUsers = pppoeUserRepository.countByActiveTrue();
            long offline = Math.max(0, totalActiveUsers - totalOnline);

            // Pending: usuários inativos no sistema (aguardando ativação)
            Long totalUsers = pppoeUserRepository.count();
            long pending = totalUsers - totalActiveUsers;

            log.info("Estatísticas de conexão em tempo real: Online={}, Offline={}, Pending={}, Total={}",
                    totalOnline, offline, pending, totalUsers);

            return new ConnectionStats(totalOnline, offline, pending);

        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas de conexão do Mikrotik: {}", e.getMessage(), e);
            // Em caso de falha total, retorna zeros (degradação graciosa)
            return new ConnectionStats(0L, 0L, 0L);
        }
    }

    @Transactional(readOnly = true)
    public ConnectionStatusDTO getConnectionStatus() {
        log.info("Fetching connection status");

        Long companyId = CompanyContextHolder.getCompanyId();

        // Busca dados em tempo real
        ConnectionStats stats = fetchRealTimeConnectionStats(companyId);

        Long activeConnections = stats.online;
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

    /**
     * Record interno para armazenar estatísticas de conexão
     */
    private record ConnectionStats(Long online, Long offline, Long pending) {
        public Long total() {
            return online + offline + pending;
        }
    }
}
