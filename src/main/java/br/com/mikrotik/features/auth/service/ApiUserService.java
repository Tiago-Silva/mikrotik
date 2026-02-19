package br.com.mikrotik.features.auth.service;
import br.com.mikrotik.features.auth.dto.ApiUserDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.infrastructure.exception.ValidationException;
import br.com.mikrotik.features.auth.model.ApiUser;
import br.com.mikrotik.features.auth.model.UserRole;
import br.com.mikrotik.features.auth.repository.ApiUserRepository;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiUserService {
    private final ApiUserRepository apiUserRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     * Criar novo usuário
     */
    @Transactional
    public ApiUserDTO create(ApiUserDTO dto) {
        log.info("Criando novo usuário: {}", dto.getUsername());
        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }
        // Validar permissões
        validatePermissions(null, dto.getRole());
        // Verificar se username já existe
        if (apiUserRepository.existsByUsernameAndCompanyId(dto.getUsername(), companyId)) {
            throw new ValidationException("Já existe um usuário com este username");
        }
        // Verificar se email já existe
        if (apiUserRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("Já existe um usuário com este email");
        }
        // Validar senha
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        }
        dto.setCompanyId(companyId);
        ApiUser user = dto.toEntity();
        // Criptografar senha
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user = apiUserRepository.save(user);
        log.info("Usuário criado com sucesso: ID={}, Username={}", user.getId(), user.getUsername());
        return ApiUserDTO.fromEntity(user);
    }
    /**
     * Buscar usuário por ID
     */
    @Transactional(readOnly = true)
    public ApiUserDTO findById(Long id) {
        log.info("Buscando usuário por ID: {}", id);
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return ApiUserDTO.fromEntity(user);
    }
    /**
     * Buscar usuário por username
     */
    @Transactional(readOnly = true)
    public ApiUserDTO findByUsername(String username) {
        log.info("Buscando usuário por username: {}", username);
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findByUsernameAndCompanyId(username, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
        return ApiUserDTO.fromEntity(user);
    }
    /**
     * Listar todos os usuários (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ApiUserDTO> findAll(Pageable pageable) {
        log.info("Listando usuários - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ApiUser> users = apiUserRepository.findByCompanyId(companyId, pageable);
        return users.map(ApiUserDTO::fromEntity);
    }
    /**
     * Listar usuários ativos
     */
    @Transactional(readOnly = true)
    public Page<ApiUserDTO> findAllActive(Pageable pageable) {
        log.info("Listando usuários ativos");
        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ApiUser> users = apiUserRepository.findByCompanyIdAndActive(companyId, true, pageable);
        return users.map(ApiUserDTO::fromEntity);
    }
    /**
     * Listar usuários por role
     */
    @Transactional(readOnly = true)
    public List<ApiUserDTO> findByRole(UserRole role) {
        log.info("Listando usuários com role: {}", role);
        Long companyId = CompanyContextHolder.getCompanyId();
        List<ApiUser> users = apiUserRepository.findByCompanyId(companyId);
        return users.stream()
                .filter(u -> u.getRole() == role)
                .map(ApiUserDTO::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Atualizar usuário
     */
    @Transactional
    public ApiUserDTO update(Long id, ApiUserDTO dto) {
        log.info("Atualizando usuário ID: {}", id);
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        // Validar permissões
        validatePermissions(user.getRole(), dto.getRole());
        // Verificar se username mudou e se já existe
        if (!user.getUsername().equals(dto.getUsername())) {
            if (apiUserRepository.existsByUsernameAndCompanyId(dto.getUsername(), companyId)) {
                throw new ValidationException("Já existe um usuário com este username");
            }
            user.setUsername(dto.getUsername());
        }
        // Verificar se email mudou e se já existe
        if (!user.getEmail().equals(dto.getEmail())) {
            apiUserRepository.findByEmail(dto.getEmail())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {
                        throw new ValidationException("Já existe um usuário com este email");
                    });
            user.setEmail(dto.getEmail());
        }
        // Atualizar senha se fornecida (apenas ADMIN pode fazer via update)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (!isCurrentUserAdmin()) {
                throw new ValidationException("Apenas administradores podem alterar senha via edição de usuário. Use o endpoint /change-password");
            }
            if (dto.getPassword().length() < 6) {
                throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            log.info("Admin {} resetou senha do usuário ID: {}", getCurrentUsername(), id);
        }
        // Atualizar campos
        user.setRole(dto.getRole() != null ? dto.getRole() : UserRole.VIEWER);
        user.setActive(dto.getActive() != null ? dto.getActive() : true);
        user = apiUserRepository.save(user);
        log.info("Usuário atualizado com sucesso: ID={}", user.getId());
        return ApiUserDTO.fromEntity(user);
    }
    /**
     * Alterar senha do próprio usuário
     */
    @Transactional
    public void changePassword(Long id, ApiUserDTO.ChangePasswordDTO dto) {
        log.info("Alterando senha do usuário ID: {}", id);
        // Validar confirmação de senha
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ValidationException("Nova senha e confirmação não conferem");
        }
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        // Verificar senha atual
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Senha atual incorreta");
        }
        // Atualizar senha
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        apiUserRepository.save(user);
        log.info("Senha alterada com sucesso para usuário ID: {}", id);
    }
    /**
     * Reset de senha (apenas admin)
     */
    @Transactional
    public void resetPassword(Long id, ApiUserDTO.ResetPasswordDTO dto) {
        log.info("Resetando senha do usuário ID: {}", id);
        // Verificar se usuário atual é admin
        if (!isCurrentUserAdmin()) {
            throw new ValidationException("Apenas administradores podem resetar senhas");
        }
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        // Atualizar senha
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        apiUserRepository.save(user);
        log.info("Senha resetada com sucesso para usuário ID: {}", id);
    }
    /**
     * Ativar/Desativar usuário
     */
    @Transactional
    public ApiUserDTO toggleActive(Long id) {
        log.info("Alterando status ativo do usuário ID: {}", id);
        // Verificar se usuário atual é admin
        if (!isCurrentUserAdmin()) {
            throw new ValidationException("Apenas administradores podem ativar/desativar usuários");
        }
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        // Não permitir desativar a si mesmo
        String currentUsername = getCurrentUsername();
        if (user.getUsername().equals(currentUsername)) {
            throw new ValidationException("Você não pode desativar sua própria conta");
        }
        user.setActive(!user.getActive());
        user = apiUserRepository.save(user);
        log.info("Status do usuário alterado: ID={}, Active={}", user.getId(), user.getActive());
        return ApiUserDTO.fromEntity(user);
    }
    /**
     * Deletar usuário (soft delete - apenas desativa)
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando usuário ID: {}", id);
        // Verificar se usuário atual é admin
        if (!isCurrentUserAdmin()) {
            throw new ValidationException("Apenas administradores podem deletar usuários");
        }
        Long companyId = CompanyContextHolder.getCompanyId();
        ApiUser user = apiUserRepository.findById(id)
                .filter(u -> u.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        // Não permitir deletar a si mesmo
        String currentUsername = getCurrentUsername();
        if (user.getUsername().equals(currentUsername)) {
            throw new ValidationException("Você não pode deletar sua própria conta");
        }
        // Soft delete - apenas desativa
        user.setActive(false);
        apiUserRepository.save(user);
        log.info("Usuário desativado (soft delete): ID={}", id);
    }
    /**
     * Atualizar último login
     */
    @Transactional
    public void updateLastLogin(String username) {
        apiUserRepository.findByUsername(username)
                .ifPresent(user -> {
                    user.updateLastLogin();
                    apiUserRepository.save(user);
                    log.debug("Último login atualizado para usuário: {}", username);
                });
    }
    // ==================== MÉTODOS AUXILIARES ====================
    /**
     * Valida permissões para criação/edição de usuários
     */
    private void validatePermissions(UserRole currentRole, UserRole newRole) {
        ApiUser currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            throw new ValidationException("Usuário atual não possui permissões válidas");
        }
        UserRole userRole = currentUser.getRole();
        // Admin pode fazer tudo
        if (userRole.isAdmin()) {
            return;
        }
        // Não-admins não podem criar outros admins
        if (newRole == UserRole.ADMIN) {
            throw new ValidationException("Você não tem permissão para criar/editar administradores");
        }
        // Não-admins não podem elevar permissões acima da própria role
        if (!userRole.hasPermissionLevel(newRole)) {
            throw new ValidationException("Você não pode atribuir uma permissão superior à sua");
        }
        // Se está editando, não pode alterar role de alguém com permissão maior ou igual
        if (currentRole != null && !userRole.hasPermissionLevel(currentRole)) {
            throw new ValidationException("Você não pode editar usuários com permissões iguais ou superiores à sua");
        }
    }
    /**
     * Obtém usuário atual do contexto de segurança (método público para controllers)
     */
    public ApiUser getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new ValidationException("Usuário não autenticado");
        }
        return apiUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + username));
    }

    /**
     * Obtém usuário atual ou null (uso interno)
     */
    private ApiUser getCurrentUserOrNull() {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        return apiUserRepository.findByUsername(username).orElse(null);
    }

    /**
     * Verifica se o usuário atual é admin
     */
    private boolean isCurrentUserAdmin() {
        ApiUser currentUser = getCurrentUserOrNull();
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Verifica se o ID fornecido é do usuário atual (usado em @PreAuthorize)
     */
    public boolean isCurrentUser(Long userId) {
        ApiUser currentUser = getCurrentUserOrNull();
        return currentUser != null && currentUser.getId().equals(userId);
    }

    /**
     * Obtém username do usuário atual
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }
}
