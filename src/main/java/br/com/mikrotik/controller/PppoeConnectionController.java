package br.com.mikrotik.controller;

import br.com.mikrotik.dto.PppoeConnectionDTO;
import br.com.mikrotik.service.PppoeConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Obter conexão por ID", description = "Retorna detalhes de uma conexão específica")
    public ResponseEntity<PppoeConnectionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar conexões de um servidor", description = "Retorna todas as conexões ativas de um servidor com paginação")
    public ResponseEntity<Page<PppoeConnectionDTO>> getByServer(@PathVariable Long serverId, Pageable pageable) {
        return ResponseEntity.ok(service.getByServer(serverId, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar conexões de um usuário", description = "Retorna todas as conexões ativas de um usuário")
    public ResponseEntity<List<PppoeConnectionDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todas as conexões", description = "Retorna lista de todas as conexões ativas")
    public ResponseEntity<List<PppoeConnectionDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/active/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar conexões ativas", description = "Retorna quantidade de conexões ativas no sistema")
    public ResponseEntity<Long> countActive() {
        return ResponseEntity.ok(service.countActive());
    }

    @GetMapping("/server/{serverId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar conexões ativas de um servidor", description = "Retorna apenas as conexões ativas de um servidor")
    public ResponseEntity<List<PppoeConnectionDTO>> getActiveByServer(@PathVariable Long serverId) {
        return ResponseEntity.ok(service.getActiveByServer(serverId));
    }
}
