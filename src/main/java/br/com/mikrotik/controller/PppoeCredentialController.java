package br.com.mikrotik.controller;

import br.com.mikrotik.dto.PppoeCredentialDTO;
import br.com.mikrotik.model.PppoeCredential;
import br.com.mikrotik.service.PppoeCredentialService;
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
@RequestMapping("/api/pppoe-credentials")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Credenciais PPPoE", description = "Gerenciamento de credenciais de acesso PPPoE")
public class PppoeCredentialController {

    private final PppoeCredentialService pppoeCredentialService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar nova credencial", description = "Cria uma nova credencial de acesso PPPoE")
    public ResponseEntity<PppoeCredentialDTO> create(@Valid @RequestBody PppoeCredentialDTO dto) {
        log.info("POST /api/pppoe-credentials - Criando nova credencial: {}", dto.getUsername());
        PppoeCredentialDTO created = pppoeCredentialService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar credencial por ID", description = "Retorna detalhes de uma credencial específica")
    public ResponseEntity<PppoeCredentialDTO> findById(@PathVariable Long id) {
        log.info("GET /api/pppoe-credentials/{} - Buscando credencial", id);
        PppoeCredentialDTO credential = pppoeCredentialService.findById(id);
        return ResponseEntity.ok(credential);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todas as credenciais", description = "Lista credenciais da empresa (paginado)")
    public ResponseEntity<Page<PppoeCredentialDTO>> findAll(
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/pppoe-credentials - Listando credenciais");
        Page<PppoeCredentialDTO> credentials = pppoeCredentialService.findAll(pageable);
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/server/{serverId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar credenciais por servidor", description = "Lista credenciais de um servidor específico")
    public ResponseEntity<Page<PppoeCredentialDTO>> findByServer(
            @PathVariable Long serverId,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/pppoe-credentials/server/{} - Listando credenciais", serverId);
        Page<PppoeCredentialDTO> credentials = pppoeCredentialService.findByServer(serverId, pageable);
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar credenciais por status", description = "Lista credenciais filtradas por status")
    public ResponseEntity<Page<PppoeCredentialDTO>> findByStatus(
            @PathVariable PppoeCredential.CredentialStatus status,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/pppoe-credentials/status/{} - Listando credenciais", status);
        Page<PppoeCredentialDTO> credentials = pppoeCredentialService.findByStatus(status, pageable);
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar credenciais não vinculadas", description = "Lista credenciais sem contrato vinculado (paginado)")
    public ResponseEntity<Page<PppoeCredentialDTO>> findUnassigned(
            @PageableDefault(size = 50, sort = "username") Pageable pageable) {
        log.info("GET /api/pppoe-credentials/unassigned - Listando credenciais não vinculadas");
        Page<PppoeCredentialDTO> credentials = pppoeCredentialService.findUnassigned(pageable);
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar com filtros", description = "Busca credenciais com múltiplos filtros opcionais")
    public ResponseEntity<Page<PppoeCredentialDTO>> findByFilters(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long serverId,
            @RequestParam(required = false) PppoeCredential.CredentialStatus status,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/pppoe-credentials/filter - Buscando com filtros");
        Page<PppoeCredentialDTO> credentials = pppoeCredentialService.findByFilters(username, serverId, status, pageable);
        return ResponseEntity.ok(credentials);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar credencial", description = "Atualiza os dados de uma credencial PPPoE")
    public ResponseEntity<PppoeCredentialDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PppoeCredentialDTO dto) {
        log.info("PUT /api/pppoe-credentials/{} - Atualizando credencial", id);
        PppoeCredentialDTO updated = pppoeCredentialService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Alterar status", description = "Altera o status de uma credencial (ONLINE, OFFLINE, DISABLED)")
    public ResponseEntity<PppoeCredentialDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam PppoeCredential.CredentialStatus status) {
        log.info("PATCH /api/pppoe-credentials/{}/status?status={}", id, status);
        PppoeCredentialDTO updated = pppoeCredentialService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar credencial", description = "Remove uma credencial PPPoE do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/pppoe-credentials/{} - Deletando credencial", id);
        pppoeCredentialService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar credenciais", description = "Retorna o número total de credenciais da empresa")
    public ResponseEntity<Long> countByCompany() {
        log.info("GET /api/pppoe-credentials/count");
        long count = pppoeCredentialService.countByCompany();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar por status", description = "Retorna o número de credenciais por status")
    public ResponseEntity<Long> countByStatus(@PathVariable PppoeCredential.CredentialStatus status) {
        log.info("GET /api/pppoe-credentials/count/status/{}", status);
        long count = pppoeCredentialService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
}
