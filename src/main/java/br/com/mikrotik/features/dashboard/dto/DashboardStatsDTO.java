package br.com.mikrotik.features.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Estatísticas do dashboard do sistema")
public class DashboardStatsDTO {
    @Schema(description = "Total de servidores Mikrotik cadastrados", example = "5")
    private Long totalServers;

    @Schema(description = "Servidores Mikrotik ativos", example = "4")
    private Long activeServers;

    @Schema(description = "Total de usuários PPPoE cadastrados", example = "1247")
    private Long totalUsers;

    @Schema(description = "Usuários PPPoE ativos", example = "1100")
    private Long activeUsers;

    @Schema(description = "Total de perfis PPPoE", example = "15")
    private Long totalProfiles;

    @Schema(description = "Conexões ativas no momento (dados em tempo real do Mikrotik)", example = "892")
    private Long onlineConnections;

    @Schema(description = "Conexões offline/desconectadas", example = "245")
    private Long offlineConnections;

    @Schema(description = "Conexões pendentes/aguardando ativação", example = "110")
    private Long pendingConnections;

    @Schema(description = "Total de conexões (online + offline + pending)", example = "1247")
    private Long totalConnections;
}
