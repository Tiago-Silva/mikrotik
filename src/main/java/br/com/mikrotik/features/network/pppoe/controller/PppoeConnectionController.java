package br.com.mikrotik.features.network.pppoe.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.network.pppoe.dto.PppoeConnectionDTO;
import br.com.mikrotik.features.network.pppoe.service.PppoeConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Conexões PPPoE", description = "Monitorar conexões ativas de PPPoE")
public class PppoeConnectionController {

    private final PppoeConnectionService service;

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Obter conexão por ID", description = "Retorna detalhes de uma conexão específica")
    public ResponseEntity<PppoeConnectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/server/{serverId}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Listar conexões de um servidor", description = "Retorna todas as conexões ativas de um servidor com paginação")
    public ResponseEntity<Page<PppoeConnectionDTO>> getByServer(@PathVariable Long serverId, Pageable pageable) {
        return ResponseEntity.ok(service.getByServer(serverId, pageable));
    }

    @GetMapping("/user/{userId}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Listar conexões de um usuário", description = "Retorna conexões paginadas de um usuário")
    public ResponseEntity<Page<PppoeConnectionDTO>> getByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "connectedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getByUser(userId, pageable));
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todas as conexões", description = "Retorna lista paginada de todas as conexões")
    public ResponseEntity<Page<PppoeConnectionDTO>> getAll(
            @PageableDefault(size = 20, sort = "connectedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active/count")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Contar conexões ativas", description = "Retorna quantidade de conexões ativas no sistema")
    public ResponseEntity<Long> countActive() {
        return ResponseEntity.ok(service.countActive());
    }

    @GetMapping("/server/{serverId}/active")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Listar conexões ativas de um servidor", description = "Retorna conexões ativas paginadas de um servidor")
    public ResponseEntity<Page<PppoeConnectionDTO>> getActiveByServer(
            @PathVariable Long serverId,
            @PageableDefault(size = 20, sort = "connectedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getActiveByServer(serverId, pageable));
    }
}
