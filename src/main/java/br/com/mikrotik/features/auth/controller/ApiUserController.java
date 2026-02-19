package br.com.mikrotik.features.auth.controller;

import br.com.mikrotik.features.auth.dto.ApiUserDTO;
import br.com.mikrotik.features.auth.dto.AvailableModulesDTO;
import br.com.mikrotik.features.auth.dto.UserPermissionDTO;
import br.com.mikrotik.features.auth.model.ApiUser;
import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.auth.model.UserRole;
import br.com.mikrotik.features.auth.service.ApiUserService;
import br.com.mikrotik.features.auth.service.PermissionService;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários da API", description = "Endpoints para gerenciamento de usuários do sistema")
public class ApiUserController {
    private final ApiUserService apiUserService;
    private final PermissionService permissionService;
    /**
     * Criar novo usuário
     */
    @PostMapping
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.CREATE)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.EDIT)
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
    @PreAuthorize("@apiUserService.isCurrentUser(#id) or hasRole('ADMIN')")
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.EDIT)
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
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.EDIT)
    @Operation(summary = "Ativar/Desativar", description = "Alterna o status ativo de um usuário")
    public ResponseEntity<ApiUserDTO> toggleActive(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("PATCH /api/users/{}/toggle-active - Alternando status", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ENDPOINTS DE PERMISSÕES CUSTOMIZADAS ====================

    /**
     * Listar módulos disponíveis no sistema
     */
    @GetMapping("/modules")
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.VIEW)
    @Operation(summary = "Listar módulos",
               description = "Lista todos os módulos e ações disponíveis no sistema")
    public ResponseEntity<AvailableModulesDTO> getAvailableModules() {
        log.info("GET /api/users/modules - Listando módulos disponíveis");
        AvailableModulesDTO modules = AvailableModulesDTO.createFull();
        return ResponseEntity.ok(modules);
    }

    /**
     * Obter permissões efetivas do usuário atual (para o frontend saber o que exibir)
     * ⚠️ ENDPOINT PÚBLICO - Todo usuário autenticado pode ver suas próprias permissões
     */
    @GetMapping("/me/permissions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Minhas permissões",
               description = "Retorna permissões efetivas do usuário logado (usado pelo frontend para controlar UI)")
    public ResponseEntity<Map<String, Object>> getMyPermissions() {
        ApiUser currentUser = apiUserService.getCurrentUser();
        log.info("GET /api/users/me/permissions - Usuário {} consultando próprias permissões", currentUser.getUsername());

        Map<SystemModule, Set<ModuleAction>> effectivePermissions = permissionService.getEffectivePermissions(currentUser);

        // Converte para formato JSON amigável
        Map<String, Object> response = new HashMap<>();
        response.put("userId", currentUser.getId());
        response.put("username", currentUser.getUsername());
        response.put("role", currentUser.getRole());
        response.put("useCustomPermissions", currentUser.getUseCustomPermissions());

        // Converte Map<SystemModule, Set<ModuleAction>> para Map<String, List<String>>
        Map<String, List<String>> permissionsMap = effectivePermissions.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().name(),
                        entry -> entry.getValue().stream().map(Enum::name).toList()
                ));

        response.put("permissions", permissionsMap);

        return ResponseEntity.ok(response);
    }

    /**
     * Obter permissões customizadas de um usuário específico (admin ou próprio usuário)
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN') or @apiUserService.isCurrentUser(#id)")
    @Operation(summary = "Obter permissões customizadas",
               description = "Retorna permissões customizadas de um usuário (somente se useCustomPermissions=true)")
    public ResponseEntity<List<UserPermissionDTO>> getUserPermissions(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("GET /api/users/{}/permissions - Obtendo permissões customizadas", id);
        List<UserPermissionDTO> permissions = permissionService.getUserPermissions(id);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Atualizar permissões customizadas de um usuário
     */
    @PutMapping("/{id}/permissions")
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar permissões",
               description = "Define permissões customizadas para um usuário (apenas ADMIN)")
    public ResponseEntity<Void> updateUserPermissions(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody Set<UserPermissionDTO> permissions) {
        log.info("PUT /api/users/{}/permissions - Atualizando permissões customizadas", id);
        permissionService.updateUserPermissions(id, permissions);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resetar permissões para padrão da role
     */
    @PostMapping("/{id}/permissions/reset")
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.EDIT)
    @Operation(summary = "Resetar permissões",
               description = "Volta as permissões do usuário para o padrão da role (apenas ADMIN)")
    public ResponseEntity<Void> resetPermissions(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        log.info("POST /api/users/{}/permissions/reset - Resetando permissões para role padrão", id);
        permissionService.resetToRolePermissions(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletar usuário (soft delete)
     */
    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.AUTH, action = ModuleAction.DELETE)
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
