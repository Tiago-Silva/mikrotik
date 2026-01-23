package br.com.mikrotik.controller;

import br.com.mikrotik.dto.IpPoolDTO;
import br.com.mikrotik.service.IpPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ip-pools")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Pools de IP", description = "Gerenciamento de pools de IP CGNAT")
public class IpPoolController {

    private final IpPoolService ipPoolService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar novo pool de IP", description = "Cria um novo pool de IP CGNAT")
    public ResponseEntity<IpPoolDTO> create(@Valid @RequestBody IpPoolDTO dto) {
        log.info("POST /api/ip-pools - Criando novo pool de IP: {}", dto.getName());
        IpPoolDTO created = ipPoolService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar pool por ID", description = "Retorna detalhes de um pool específico")
    public ResponseEntity<IpPoolDTO> findById(@PathVariable Long id) {
        log.info("GET /api/ip-pools/{} - Buscando pool", id);
        IpPoolDTO pool = ipPoolService.findById(id);
        return ResponseEntity.ok(pool);
    }

    @GetMapping("/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar pools por servidor", description = "Lista pools de IP de um servidor específico (paginado)")
    public ResponseEntity<Page<IpPoolDTO>> findByServer(
            @PathVariable Long serverId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/ip-pools/server/{} - Listando pools", serverId);
        Page<IpPoolDTO> pools = ipPoolService.findByServer(serverId, pageable);
        return ResponseEntity.ok(pools);
    }

    @GetMapping("/server/{serverId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar pools ativos por servidor", description = "Retorna apenas pools ativos de um servidor")
    public ResponseEntity<Page<IpPoolDTO>> findByServerAndActive(
            @PathVariable Long serverId,
            @RequestParam(defaultValue = "true") Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/ip-pools/server/{}/active?active={}", serverId, active);
        Page<IpPoolDTO> pools = ipPoolService.findByServerAndActive(serverId, active, pageable);
        return ResponseEntity.ok(pools);
    }

    @GetMapping("/server/{serverId}/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos os pools", description = "Lista todos os pools de um servidor (sem paginação)")
    public ResponseEntity<List<IpPoolDTO>> findAllByServer(@PathVariable Long serverId) {
        log.info("GET /api/ip-pools/server/{}/all - Listando todos os pools", serverId);
        List<IpPoolDTO> pools = ipPoolService.findAllByServer(serverId);
        return ResponseEntity.ok(pools);
    }

    @GetMapping("/server/{serverId}/active-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar pools ativos", description = "Lista apenas pools ativos de um servidor (sem paginação)")
    public ResponseEntity<List<IpPoolDTO>> findActiveByServer(@PathVariable Long serverId) {
        log.info("GET /api/ip-pools/server/{}/active-list - Listando pools ativos", serverId);
        List<IpPoolDTO> pools = ipPoolService.findActiveByServer(serverId);
        return ResponseEntity.ok(pools);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar pool", description = "Atualiza os dados de um pool de IP")
    public ResponseEntity<IpPoolDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody IpPoolDTO dto) {
        log.info("PUT /api/ip-pools/{} - Atualizando pool", id);
        IpPoolDTO updated = ipPoolService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Ativar/Desativar pool", description = "Altera o status ativo do pool")
    public ResponseEntity<IpPoolDTO> toggleActive(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        log.info("PATCH /api/ip-pools/{}/active?active={}", id, active);
        IpPoolDTO updated = ipPoolService.toggleActive(id, active);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar pool", description = "Remove um pool de IP do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/ip-pools/{} - Deletando pool", id);
        ipPoolService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/server/{serverId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar pools", description = "Retorna o número total de pools de um servidor")
    public ResponseEntity<Long> countByServer(@PathVariable Long serverId) {
        log.info("GET /api/ip-pools/server/{}/count", serverId);
        long count = ipPoolService.countByServer(serverId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/server/{serverId}/count-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar pools ativos", description = "Retorna o número de pools ativos de um servidor")
    public ResponseEntity<Long> countActiveByServer(@PathVariable Long serverId) {
        log.info("GET /api/ip-pools/server/{}/count-active", serverId);
        long count = ipPoolService.countActiveByServer(serverId);
        return ResponseEntity.ok(count);
    }
}
