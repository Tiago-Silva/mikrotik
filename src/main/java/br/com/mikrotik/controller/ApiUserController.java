package br.com.mikrotik.controller;
import br.com.mikrotik.dto.ApiUserDTO;
import br.com.mikrotik.model.UserRole;
import br.com.mikrotik.service.ApiUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários da API", description = "Endpoints para gerenciamento de usuários do sistema")
public class ApiUserController {
    private final ApiUserService apiUserService;
    /**
     * Criar novo usuário
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    public ResponseEntity<ApiUserDTO> create(@Valid @RequestBody ApiUserDTO dto) {
        log.info("POST /api/users - Criando usuário: {}", dto.getUsername());
        ApiUserDTO created = apiUserService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    /**
     * Buscar usuário por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna detalhes de um usuário específico")
    public ResponseEntity<ApiUserDTO> findById(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("GET /api/users/{} - Buscando usuário", id);
        ApiUserDTO user = apiUserService.findById(id);
        return ResponseEntity.ok(user);
    }
    /**
     * Buscar usuário por username
     */
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Buscar por username", description = "Retorna usuário pelo username")
    public ResponseEntity<ApiUserDTO> findByUsername(
            @Parameter(description = "Username do usuário") @PathVariable String username) {
        log.info("GET /api/users/username/{} - Buscando usuário", username);
        ApiUserDTO user = apiUserService.findByUsername(username);
        return ResponseEntity.ok(user);
    }
    /**
     * Listar todos os usuários
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários com paginação")
    public ResponseEntity<Page<ApiUserDTO>> findAll(
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/users - Listando usuários");
        Page<ApiUserDTO> users = apiUserService.findAll(pageable);
        return ResponseEntity.ok(users);
    }
    /**
     * Listar usuários ativos
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar usuários ativos", description = "Lista apenas usuários ativos")
    public ResponseEntity<Page<ApiUserDTO>> findAllActive(
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/users/active - Listando usuários ativos");
        Page<ApiUserDTO> users = apiUserService.findAllActive(pageable);
        return ResponseEntity.ok(users);
    }
    /**
     * Listar usuários por role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Listar por role", description = "Lista usuários de uma role específica")
    public ResponseEntity<List<ApiUserDTO>> findByRole(
            @Parameter(description = "Role do usuário") @PathVariable UserRole role) {
        log.info("GET /api/users/role/{} - Listando usuários", role);
        List<ApiUserDTO> users = apiUserService.findByRole(role);
        return ResponseEntity.ok(users);
    }
    /**
     * Listar todas as roles disponíveis
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar roles", description = "Lista todas as roles disponíveis no sistema")
    public ResponseEntity<List<RoleInfo>> getRoles() {
        log.info("GET /api/users/roles - Listando roles");
        List<RoleInfo> roles = Arrays.stream(UserRole.values())
                .map(role -> new RoleInfo(
                        role.name(),
                        role.getDisplayName(),
                        role.getDescription(),
                        role.getLevel()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }
    /**
     * Atualizar usuário
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar usuário", description = "Atualiza dados de um usuário")
    public ResponseEntity<ApiUserDTO> update(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody ApiUserDTO dto) {
        log.info("PUT /api/users/{} - Atualizando usuário", id);
        ApiUserDTO updated = apiUserService.update(id, dto);
        return ResponseEntity.ok(updated);
    }
    /**
     * Alterar senha do próprio usuário
     */
    @PatchMapping("/{id}/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCIAL', 'TECHNICAL', 'VIEWER')")
    @Operation(summary = "Alterar senha", description = "Permite ao usuário alterar sua própria senha")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody ApiUserDTO.ChangePasswordDTO dto) {
        log.info("PATCH /api/users/{}/change-password - Alterando senha", id);
        apiUserService.changePassword(id, dto);
        return ResponseEntity.noContent().build();
    }
    /**
     * Reset de senha (apenas admin)
     */
    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resetar senha", description = "Permite ao admin resetar senha de qualquer usuário")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody ApiUserDTO.ResetPasswordDTO dto) {
        log.info("PATCH /api/users/{}/reset-password - Resetando senha", id);
        apiUserService.resetPassword(id, dto);
        return ResponseEntity.noContent().build();
    }
    /**
     * Ativar/Desativar usuário
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar/Desativar", description = "Alterna o status ativo de um usuário")
    public ResponseEntity<ApiUserDTO> toggleActive(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("PATCH /api/users/{}/toggle-active - Alternando status", id);
        ApiUserDTO updated = apiUserService.toggleActive(id);
        return ResponseEntity.ok(updated);
    }
    /**
     * Deletar usuário (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar usuário", description = "Desativa um usuário (soft delete)")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deletando usuário", id);
        apiUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
    // DTO auxiliar para listagem de roles
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RoleInfo {
        private String name;
        private String displayName;
        private String description;
        private int level;
    }
}
