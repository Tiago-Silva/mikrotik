package br.com.mikrotik.controller;

import br.com.mikrotik.dto.InternetProfileDTO;
import br.com.mikrotik.service.InternetProfileService;
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
@RequestMapping("/api/internet-profiles")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Perfis de Internet", description = "Gerenciamento de perfis técnicos de internet")
public class InternetProfileController {

    private final InternetProfileService internetProfileService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar novo perfil", description = "Cria um novo perfil técnico de internet")
    public ResponseEntity<InternetProfileDTO> create(@Valid @RequestBody InternetProfileDTO dto) {
        log.info("POST /api/internet-profiles - Criando novo perfil: {}", dto.getName());
        InternetProfileDTO created = internetProfileService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar perfil por ID", description = "Retorna detalhes de um perfil específico")
    public ResponseEntity<InternetProfileDTO> findById(@PathVariable Long id) {
        log.info("GET /api/internet-profiles/{} - Buscando perfil", id);
        InternetProfileDTO profile = internetProfileService.findById(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos os perfis", description = "Lista perfis de internet da empresa (paginado)")
    public ResponseEntity<Page<InternetProfileDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/internet-profiles - Listando perfis");
        Page<InternetProfileDTO> profiles = internetProfileService.findAll(pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar perfis por servidor", description = "Lista perfis de um servidor específico (paginado)")
    public ResponseEntity<Page<InternetProfileDTO>> findByServer(
            @PathVariable Long serverId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/internet-profiles/server/{} - Listando perfis", serverId);
        Page<InternetProfileDTO> profiles = internetProfileService.findByServer(serverId, pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar perfis ativos", description = "Lista perfis ativos da empresa (paginado)")
    public ResponseEntity<Page<InternetProfileDTO>> findByActive(
            @RequestParam(defaultValue = "true") Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/internet-profiles/active?active={}", active);
        Page<InternetProfileDTO> profiles = internetProfileService.findByActive(active, pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/server/{serverId}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar perfis ativos por servidor", description = "Lista perfis ativos de um servidor (paginado)")
    public ResponseEntity<Page<InternetProfileDTO>> findByServerAndActive(
            @PathVariable Long serverId,
            @RequestParam(defaultValue = "true") Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/internet-profiles/server/{}/active?active={}", serverId, active);
        Page<InternetProfileDTO> profiles = internetProfileService.findByServerAndActive(serverId, active, pageable);
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos (sem paginação)", description = "Lista todos os perfis da empresa")
    public ResponseEntity<List<InternetProfileDTO>> findAllByCompany() {
        log.info("GET /api/internet-profiles/all - Listando todos os perfis");
        List<InternetProfileDTO> profiles = internetProfileService.findAllByCompany();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/active-list")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar ativos (sem paginação)", description = "Lista perfis ativos da empresa")
    public ResponseEntity<List<InternetProfileDTO>> findActiveByCompany() {
        log.info("GET /api/internet-profiles/active-list - Listando perfis ativos");
        List<InternetProfileDTO> profiles = internetProfileService.findActiveByCompany();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar com filtros", description = "Busca perfis com múltiplos filtros opcionais")
    public ResponseEntity<Page<InternetProfileDTO>> findByFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/internet-profiles/filter - Buscando com filtros");
        Page<InternetProfileDTO> profiles = internetProfileService.findByFilters(name, serverId, active, pageable);
        return ResponseEntity.ok(profiles);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar perfil", description = "Atualiza os dados de um perfil de internet")
    public ResponseEntity<InternetProfileDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody InternetProfileDTO dto) {
        log.info("PUT /api/internet-profiles/{} - Atualizando perfil", id);
        InternetProfileDTO updated = internetProfileService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Ativar/Desativar perfil", description = "Altera o status ativo do perfil")
    public ResponseEntity<InternetProfileDTO> toggleActive(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        log.info("PATCH /api/internet-profiles/{}/active?active={}", id, active);
        InternetProfileDTO updated = internetProfileService.toggleActive(id, active);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar perfil", description = "Remove um perfil de internet do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/internet-profiles/{} - Deletando perfil", id);
        internetProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar perfis", description = "Retorna o número total de perfis da empresa")
    public ResponseEntity<Long> countByCompany() {
        log.info("GET /api/internet-profiles/count");
        long count = internetProfileService.countByCompany();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar perfis ativos", description = "Retorna o número de perfis ativos da empresa")
    public ResponseEntity<Long> countActiveByCompany() {
        log.info("GET /api/internet-profiles/count-active");
        long count = internetProfileService.countActiveByCompany();
        return ResponseEntity.ok(count);
    }
}
