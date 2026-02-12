package br.com.mikrotik.controller;

import br.com.mikrotik.dto.PppoeUserDTO;
import br.com.mikrotik.dto.SyncResultDTO;
import br.com.mikrotik.service.PppoeUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pppoe-users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Usuários PPPoE", description = "Gerenciar usuários PPPoE")
public class PppoeUserController {

    private final PppoeUserService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar novo usuário", description = "Criar novo usuário PPPoE no servidor Mikrotik")
    public ResponseEntity<PppoeUserDTO> create(@Valid @RequestBody PppoeUserDTO dto) {
        log.info("Criando novo usuário PPPoE: {}", dto.getUsername());
        PppoeUserDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Obter usuário por ID", description = "Retorna detalhes de um usuário específico")
    public ResponseEntity<PppoeUserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar usuários de um servidor", description = "Retorna todos os usuários de um servidor Mikrotik com paginação")
    public ResponseEntity<Page<PppoeUserDTO>> getByServer(@PathVariable Long serverId, Pageable pageable) {
        return ResponseEntity.ok(service.getByServer(serverId, pageable));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos os usuários", description = "Retorna lista paginada de todos os usuários PPPoE")
    public ResponseEntity<Page<PppoeUserDTO>> getAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar usuários por status", description = "Retorna usuários filtrados por status (ONLINE, OFFLINE, DISABLED)")
    public ResponseEntity<Page<PppoeUserDTO>> getByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Buscando usuários com status: {}", status);
        return ResponseEntity.ok(service.getByStatus(status, pageable));
    }

    @GetMapping("/profile/{profileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(
        summary = "Listar usuários por perfil",
        description = "Retorna todos os usuários vinculados a um perfil PPPoE específico com paginação"
    )
    public ResponseEntity<Page<PppoeUserDTO>> getByProfile(
            @PathVariable Long profileId,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/users/profile/{}", profileId);
        return ResponseEntity.ok(service.getByProfile(profileId, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(
        summary = "Buscar usuários",
        description = "Busca usuários por username ou comentário (case-insensitive). Use o parâmetro 'q' para o termo de busca."
    )
    public ResponseEntity<Page<PppoeUserDTO>> searchUsers(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/users/search?q={}", q);
        return ResponseEntity.ok(service.searchUsers(q, pageable));
    }

    @GetMapping("/profile/{profileId}/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(
        summary = "Buscar usuários em um perfil específico",
        description = "Busca usuários por username ou comentário dentro de um perfil PPPoE específico (case-insensitive)"
    )
    public ResponseEntity<Page<PppoeUserDTO>> searchUsersByProfile(
            @PathVariable Long profileId,
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/users/profile/{}/search?q={}", profileId, q);
        return ResponseEntity.ok(service.searchUsersByProfile(profileId, q, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar usuário", description = "Modificar dados de um usuário existente")
    public ResponseEntity<PppoeUserDTO> update(@PathVariable Long id, @Valid @RequestBody PppoeUserDTO dto) {
        log.info("Atualizando usuário PPPoE: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar usuário", description = "Remover um usuário PPPoE do servidor Mikrotik")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deletando usuário PPPoE: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Desativar usuário", description = "Desativar um usuário PPPoE sem deletá-lo")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        log.info("Desativando usuário PPPoE: {}", id);
        service.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Ativar usuário", description = "Reativar um usuário PPPoE desativado")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        log.info("Ativando usuário PPPoE: {}", id);
        service.enable(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(
        summary = "Sincronizar usuários do Mikrotik",
        description = "Importa todos os usuários PPPoE existentes no servidor Mikrotik para o banco de dados. " +
                      "Usuários que já existem no banco serão ignorados. " +
                      "Se forceProfileId for informado, todos os usuários serão vinculados a esse perfil. " +
                      "Se não informado, mantém os perfis originais do MikroTik (busca por nome no banco)."
    )
    public ResponseEntity<SyncResultDTO> syncFromMikrotik(
            @PathVariable Long serverId,
            @RequestParam(required = false) Long forceProfileId) {
        log.info("Iniciando sincronização de usuários do servidor {} com perfil forçado: {}", serverId, forceProfileId);
        SyncResultDTO result = service.syncUsersFromMikrotik(serverId, forceProfileId);
        return ResponseEntity.ok(result);
    }
}
