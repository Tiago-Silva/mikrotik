package br.com.mikrotik.features.network.server.controller;

import br.com.mikrotik.shared.infrastructure.config.MikrotikConnectionConfig;
import br.com.mikrotik.features.network.server.adapter.MikrotikSshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mikrotik-debug")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mikrotik Debug", description = "Endpoints de debug para Mikrotik (uso interno)")
public class MikrotikDebugController {

    private final MikrotikConnectionConfig mikrotikConfig;
    private final MikrotikSshService sshService;

    @GetMapping("/pppoe-active-raw")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Operation(summary = "Listar PPPoE ativos (raw)", description = "Retorna o output bruto do comando /ppp active print no Mikrotik")
    public ResponseEntity<List<String>> getPppoeActiveRaw() {
        List<String> output = sshService.listActivePppoeConnections(
                mikrotikConfig.getHost(),
                mikrotikConfig.getPort(),
                mikrotikConfig.getUsername(),
                mikrotikConfig.getPassword()
        );
        return ResponseEntity.ok(output);
    }
}
