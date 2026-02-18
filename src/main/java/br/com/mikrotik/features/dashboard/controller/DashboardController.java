package br.com.mikrotik.features.dashboard.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.network.server.dto.ConnectionStatusDTO;
import br.com.mikrotik.features.dashboard.dto.DashboardStatsDTO;
import br.com.mikrotik.features.dashboard.service.DashboardService;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Dashboard", description = "Estatísticas e status do sistema")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @RequireModuleAccess(module = SystemModule.DASHBOARD, action = ModuleAction.VIEW)
    @Operation(summary = "Obter estatísticas do dashboard", description = "Retorna estatísticas gerais do sistema incluindo servidores, usuários, perfis e conexões")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        log.info("GET /api/dashboard/stats - Fetching dashboard statistics");
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/connection-status")
    @RequireModuleAccess(module = SystemModule.DASHBOARD, action = ModuleAction.VIEW)
    @Operation(summary = "Obter status das conexões", description = "Retorna o status atual das conexões do sistema")
    public ResponseEntity<ConnectionStatusDTO> getConnectionStatus() {
        log.info("GET /api/dashboard/connection-status - Fetching connection status");
        return ResponseEntity.ok(dashboardService.getConnectionStatus());
    }
}
