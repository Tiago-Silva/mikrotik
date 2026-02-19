package br.com.mikrotik.features.network.pppoe.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.network.pppoe.dto.PppoeProfileDTO;
import br.com.mikrotik.features.sync.dto.SyncResultDTO;
import br.com.mikrotik.features.network.pppoe.service.PppoeProfileService;
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
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.CREATE)
    @Operation(summary = "Criar novo perfil", description = "Criar novo perfil de PPPoE com limites de banda")
    public ResponseEntity<PppoeProfileDTO> create(@Valid @RequestBody PppoeProfileDTO dto) {
        log.info("Criando novo perfil PPPoE: {}", dto.getName());
        PppoeProfileDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Obter perfil por ID", description = "Retorna detalhes de um perfil específico")
    public ResponseEntity<PppoeProfileDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/server/{serverId}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Listar perfis de um servidor", description = "Retorna perfis paginados de um servidor Mikrotik")
    public ResponseEntity<Page<PppoeProfileDTO>> getByServer(
            @PathVariable Long serverId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.getByServer(serverId, pageable));
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todos os perfis", description = "Retorna lista paginada de todos os perfis PPPoE")
    public ResponseEntity<Page<PppoeProfileDTO>> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar perfil", description = "Modificar dados de um perfil existente")
    public ResponseEntity<PppoeProfileDTO> update(@PathVariable Long id, @Valid @RequestBody PppoeProfileDTO dto) {
        log.info("Atualizando perfil PPPoE: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.DELETE)
    @Operation(summary = "Deletar perfil", description = "Remover um perfil PPPoE da plataforma")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deletando perfil PPPoE: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync/server/{serverId}")
    @RequireModuleAccess(module = SystemModule.NETWORK, action = ModuleAction.CREATE)
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
