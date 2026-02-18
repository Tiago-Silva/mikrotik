package br.com.mikrotik.features.auth.service;

import br.com.mikrotik.features.auth.dto.UserPermissionDTO;
import br.com.mikrotik.features.auth.model.*;
import br.com.mikrotik.features.auth.repository.ApiUserRepository;
import br.com.mikrotik.features.auth.repository.UserPermissionRepository;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.infrastructure.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de permissões granulares de usuários
 * Implementa lógica híbrida: Role padrão OU permissões customizadas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final UserPermissionRepository permissionRepository;
    private final ApiUserRepository userRepository;

    // ==================== VERIFICAÇÃO DE ACESSO ====================

    /**
     * Verifica se usuário tem acesso a um módulo com uma ação específica
     * Lógica híbrida: se useCustomPermissions=false, usa role; senão, consulta permissões
     */
    @Cacheable(value = "userPermissions", key = "#userId + '-' + #module + '-' + #action")
    public boolean hasModuleAccess(Long userId, SystemModule module, ModuleAction action) {
        ApiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // ADMIN sempre tem acesso total
        if (user.getRole() == UserRole.ADMIN) {
            log.debug("Usuário {} é ADMIN - acesso concedido a {} com {}", userId, module, action);
            return true;
        }

        // Se usa permissões customizadas, consulta tabela user_permissions
        if (Boolean.TRUE.equals(user.getUseCustomPermissions())) {
            return hasCustomPermission(userId, module, action);
        }

        // Caso contrário, usa permissões da role padrão
        return hasRolePermission(user.getRole(), module, action);
    }

    /**
     * Verifica permissão customizada (tabela user_permissions)
     */
    private boolean hasCustomPermission(Long userId, SystemModule module, ModuleAction action) {
        Optional<UserPermission> permission = permissionRepository.findByUserIdAndModule(userId, module);

        if (permission.isEmpty()) {
            log.debug("Usuário {} não tem permissão customizada para módulo {}", userId, module);
            return false;
        }

        boolean hasAction = permission.get().hasAction(action);
        log.debug("Usuário {} {} permissão {} no módulo {}",
                userId, hasAction ? "tem" : "não tem", action, module);
        return hasAction;
    }

    /**
     * Verifica permissão baseada na role padrão
     */
    private boolean hasRolePermission(UserRole role, SystemModule module, ModuleAction action) {
        // Módulos permitidos para a role
        Set<SystemModule> allowedModules = new HashSet<>(Arrays.asList(SystemModule.getDefaultModulesForRole(role)));

        if (!allowedModules.contains(module)) {
            log.debug("Role {} não tem acesso ao módulo {}", role, module);
            return false;
        }

        // Ações permitidas para a role
        Set<ModuleAction> allowedActions = getDefaultActionsForRole(role);
        boolean hasAction = allowedActions.contains(action);

        log.debug("Role {} {} permissão {} no módulo {}",
                role, hasAction ? "tem" : "não tem", action, module);
        return hasAction;
    }

    /**
     * Retorna ações padrão para cada role
     */
    private Set<ModuleAction> getDefaultActionsForRole(UserRole role) {
        return switch (role) {
            case ADMIN -> new HashSet<>(Arrays.asList(ModuleAction.getAdminActions()));
            case OPERATOR, FINANCIAL, TECHNICAL -> new HashSet<>(Arrays.asList(ModuleAction.getOperatorActions()));
            case VIEWER -> new HashSet<>(Arrays.asList(ModuleAction.getViewerActions()));
        };
    }

    // ==================== CONSULTA DE PERMISSÕES ====================

    /**
     * Retorna todos os módulos que o usuário tem acesso
     */
    @Cacheable(value = "userModules", key = "#userId")
    public Map<SystemModule, Set<ModuleAction>> getUserModules(Long userId) {
        ApiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // ADMIN tem acesso total
        if (user.getRole() == UserRole.ADMIN) {
            return getAllModulesWithFullAccess();
        }

        // Se usa permissões customizadas
        if (Boolean.TRUE.equals(user.getUseCustomPermissions())) {
            return getCustomUserModules(userId);
        }

        // Usa permissões da role
        return getRoleModules(user.getRole());
    }

    /**
     * Retorna permissões efetivas do usuário (híbrido)
     */
    public Map<SystemModule, Set<ModuleAction>> getEffectivePermissions(ApiUser user) {
        if (user.getRole() == UserRole.ADMIN) {
            return getAllModulesWithFullAccess();
        }

        if (Boolean.TRUE.equals(user.getUseCustomPermissions())) {
            return getCustomUserModules(user.getId());
        }

        return getRoleModules(user.getRole());
    }

    /**
     * Retorna módulos de permissões customizadas
     */
    private Map<SystemModule, Set<ModuleAction>> getCustomUserModules(Long userId) {
        List<UserPermission> permissions = permissionRepository.findByUserId(userId);

        return permissions.stream()
                .collect(Collectors.toMap(
                        UserPermission::getModule,
                        UserPermission::getActions,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Retorna módulos baseado na role
     */
    private Map<SystemModule, Set<ModuleAction>> getRoleModules(UserRole role) {
        SystemModule[] modules = SystemModule.getDefaultModulesForRole(role);
        Set<ModuleAction> actions = getDefaultActionsForRole(role);

        Map<SystemModule, Set<ModuleAction>> result = new HashMap<>();
        for (SystemModule module : modules) {
            result.put(module, new HashSet<>(actions));
        }
        return result;
    }

    /**
     * Retorna todos os módulos com acesso total (ADMIN)
     */
    private Map<SystemModule, Set<ModuleAction>> getAllModulesWithFullAccess() {
        Map<SystemModule, Set<ModuleAction>> result = new HashMap<>();
        Set<ModuleAction> allActions = new HashSet<>(Arrays.asList(ModuleAction.values()));

        for (SystemModule module : SystemModule.values()) {
            result.put(module, new HashSet<>(allActions));
        }
        return result;
    }

    // ==================== GERENCIAMENTO DE PERMISSÕES ====================

    /**
     * Atualiza permissões customizadas de um usuário
     * ⚠️ CRÍTICO: Invalida cache após atualização
     */
    @Transactional
    @CacheEvict(value = {"userPermissions", "userModules"}, allEntries = true)
    public void updateUserPermissions(Long userId, Set<UserPermissionDTO> permissionsDTO) {
        ApiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // ADMIN não pode ter permissões customizadas
        if (user.getRole() == UserRole.ADMIN) {
            throw new ValidationException("ADMIN sempre tem acesso total - não é possível customizar");
        }

        // Validar que há pelo menos uma permissão
        if (permissionsDTO == null || permissionsDTO.isEmpty()) {
            throw new ValidationException("Usuário deve ter pelo menos um módulo com permissão VIEW");
        }

        // Validar que há pelo menos uma permissão VIEW
        boolean hasViewPermission = permissionsDTO.stream()
                .anyMatch(p -> p.getActions() != null && p.getActions().contains(ModuleAction.VIEW));

        if (!hasViewPermission) {
            throw new ValidationException("Usuário deve ter pelo menos um módulo com permissão VIEW");
        }

        // Remover permissões antigas (dentro da mesma transação)
        List<UserPermission> oldPermissions = permissionRepository.findByUserId(userId);
        if (!oldPermissions.isEmpty()) {
            permissionRepository.deleteAll(oldPermissions);
            permissionRepository.flush(); // Força execução do DELETE antes dos INSERTs
        }

        // Criar novas permissões
        List<UserPermission> newPermissions = permissionsDTO.stream()
                .map(dto -> {
                    UserPermission permission = new UserPermission();
                    permission.setUser(user);
                    permission.setModule(dto.getModule());
                    permission.setActions(dto.getActions());
                    permission.setSubModules(dto.getSubModules());
                    return permission;
                })
                .toList();

        permissionRepository.saveAll(newPermissions);

        // Ativar uso de permissões customizadas
        user.setUseCustomPermissions(true);
        userRepository.save(user);

        log.info("Permissões customizadas atualizadas para usuário {}: {} módulos",
                userId, newPermissions.size());
    }

    /**
     * Reseta permissões para padrão da role
     * ⚠️ CRÍTICO: Invalida cache após reset
     */
    @Transactional
    @CacheEvict(value = {"userPermissions", "userModules"}, allEntries = true)
    public void resetToRolePermissions(Long userId) {
        ApiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Remover permissões customizadas
        permissionRepository.deleteByUserId(userId);

        // Desativar uso de permissões customizadas
        user.setUseCustomPermissions(false);
        userRepository.save(user);

        log.info("Permissões resetadas para role padrão: usuário {} ({})", userId, user.getRole());
    }

    /**
     * Retorna lista de permissões customizadas do usuário
     */
    public List<UserPermissionDTO> getUserPermissions(Long userId) {
        List<UserPermission> permissions = permissionRepository.findByUserId(userId);

        return permissions.stream()
                .map(UserPermissionDTO::fromEntity)
                .toList();
    }

    /**
     * Limpa todo o cache de permissões
     * Útil para forçar recarga após mudanças administrativas
     * ⚠️ CRÍTICO: Use com moderação - impacta performance
     */
    @CacheEvict(value = {"userPermissions", "userModules"}, allEntries = true)
    public void clearAllPermissionsCache() {
        log.info("🔄 Cache de permissões limpo manualmente");
    }
}

