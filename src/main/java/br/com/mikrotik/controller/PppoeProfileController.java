package br.com.mikrotik.controller;

import br.com.mikrotik.dto.PppoeProfileDTO;
import br.com.mikrotik.dto.SyncResultDTO;
import br.com.mikrotik.service.PppoeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Perfis PPPoE", description = "Gerenciar perfis/planos de PPPoE")
public class PppoeProfileController {

    private final PppoeProfileService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar novo perfil", description = "Criar novo perfil de PPPoE com limites de banda")
    public ResponseEntity<PppoeProfileDTO> create(@Valid @RequestBody PppoeProfileDTO dto) {
        log.info("Criando novo perfil PPPoE: {}", dto.getName());
        PppoeProfileDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Obter perfil por ID", description = "Retorna detalhes de um perfil específico")
    public ResponseEntity<PppoeProfileDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar perfis de um servidor", description = "Retorna todos os perfis de um servidor Mikrotik")
    public ResponseEntity<List<PppoeProfileDTO>> getByServer(@PathVariable Long serverId) {
        return ResponseEntity.ok(service.getByServer(serverId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos os perfis", description = "Retorna lista de todos os perfis PPPoE")
    public ResponseEntity<List<PppoeProfileDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar perfil", description = "Modificar dados de um perfil existente")
    public ResponseEntity<PppoeProfileDTO> update(@PathVariable Long id, @Valid @RequestBody PppoeProfileDTO dto) {
        log.info("Atualizando perfil PPPoE: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar perfil", description = "Remover um perfil PPPoE da plataforma")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deletando perfil PPPoE: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(
        summary = "Sincronizar profiles do Mikrotik",
        description = "Importa todos os profiles PPPoE existentes no servidor Mikrotik para o banco de dados. " +
                      "Profiles que já existem no banco serão ignorados."
    )
    public ResponseEntity<SyncResultDTO> syncFromMikrotik(@PathVariable Long serverId) {
        log.info("Iniciando sincronização de profiles do servidor {}", serverId);
        SyncResultDTO result = service.syncProfilesFromMikrotik(serverId);
        return ResponseEntity.ok(result);
    }
}
