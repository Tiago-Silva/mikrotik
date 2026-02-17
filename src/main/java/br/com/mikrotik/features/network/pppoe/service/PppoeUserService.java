package br.com.mikrotik.features.network.pppoe.service;

import br.com.mikrotik.features.network.pppoe.dto.MikrotikPppoeUserDTO;
import br.com.mikrotik.features.network.pppoe.dto.PppoeUserDTO;
import br.com.mikrotik.features.sync.dto.SyncResultDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeProfileRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PppoeUserService {

    private final PppoeUserRepository repository;
    private final MikrotikServerRepository serverRepository;
    private final PppoeProfileRepository profileRepository;
    private final MikrotikApiService apiService; // API service for better performance

    /**
     * Buscar todos os usuários PPPoE da empresa
     */
    @Transactional(readOnly = true)
    public List<PppoeUser> findAll() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return repository.findByCompanyId(companyId);
    }

    @Transactional
    public PppoeUserDTO create(PppoeUserDTO dto) {
        MikrotikServer server = serverRepository.findById(dto.getMikrotikServerId())
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));

        PppoeProfile profile = profileRepository.findById(dto.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado"));

        // Verificar se username já existe
        if (repository.findByUsernameAndMikrotikServer(dto.getUsername(), server).isPresent()) {
            throw new IllegalArgumentException("Username já existe neste servidor");
        }

        // Criar usuário no Mikrotik
        // 1. CRIAR NO MIKROTIK VIA API
        apiService.createPppoeUser(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                dto.getUsername(),
                dto.getPassword(),
                profile.getName()
        );

        PppoeUser user = new PppoeUser();
        user.setCompanyId(server.getCompanyId());
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // Armazenar em texto plano (técnicos precisam visualizar)
        user.setEmail(dto.getEmail());
        user.setComment(dto.getComment());
        user.setMacAddress(dto.getMacAddress());
        user.setStaticIp(dto.getStaticIp());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : PppoeUser.UserStatus.OFFLINE);
        user.setActive(dto.getActive() != null ? dto.getActive() : true);
        user.setProfile(profile);
        user.setMikrotikServer(server);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        PppoeUser saved = repository.save(user);
        log.info("Usuário PPPoE criado: {}", saved.getId());
        return mapToDTO(saved);
    }

    public PppoeUserDTO getById(Long id) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));
        return mapToDTO(user);
    }

    public Page<PppoeUserDTO> getByServer(Long serverId, Pageable pageable) {
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));
        return repository.findByMikrotikServer(server, pageable)
                .map(this::mapToDTO);
    }

    public Page<PppoeUserDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDTO);
    }

    public Page<PppoeUserDTO> getByStatus(String statusStr, Pageable pageable) {
        try {
            PppoeUser.UserStatus status = PppoeUser.UserStatus.valueOf(statusStr.toUpperCase());
            Long companyId = CompanyContextHolder.getCompanyId();
            return repository.findByCompanyIdAndStatus(companyId, status, pageable)
                    .map(this::mapToDTO);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + statusStr + ". Use: ONLINE, OFFLINE ou DISABLED");
        }
    }

    public Page<PppoeUserDTO> getByProfile(Long profileId, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        log.info("Buscando usuários PPPoE por perfil ID: {} para company: {}", profileId, companyId);

        // Validar se o perfil existe
        profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + profileId));

        return repository.findByCompanyIdAndProfileId(companyId, profileId, pageable)
                .map(this::mapToDTO);
    }

    public Page<PppoeUserDTO> searchUsers(String searchTerm, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        log.info("Buscando usuários PPPoE com termo: '{}' para company: {}", searchTerm, companyId);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Termo de busca não pode ser vazio");
        }

        String term = searchTerm.trim();
        return repository.findByCompanyIdAndUsernameContainingIgnoreCaseOrCommentContainingIgnoreCase(
                companyId, term, term, pageable)
                .map(this::mapToDTO);
    }

    public Page<PppoeUserDTO> searchUsersByProfile(Long profileId, String searchTerm, Pageable pageable) {
        Long companyId = CompanyContextHolder.getCompanyId();
        log.info("Buscando usuários PPPoE por perfil {} com termo: '{}' para company: {}",
                profileId, searchTerm, companyId);

        // Validar se o perfil existe
        profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + profileId));

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new IllegalArgumentException("Termo de busca não pode ser vazio");
        }

        String term = searchTerm.trim();
        return repository.findByCompanyIdAndProfileIdAndUsernameContainingIgnoreCaseOrCompanyIdAndProfileIdAndCommentContainingIgnoreCase(
                companyId, profileId, term, companyId, profileId, term, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public PppoeUserDTO update(Long id, PppoeUserDTO dto) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        PppoeProfile newProfile = profileRepository.findById(dto.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado"));

        MikrotikServer server = user.getMikrotikServer();

        apiService.changePppoeUserAll(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                user,
                newProfile
        );

        // 2. ATUALIZAR NO BANCO
        user.setEmail(dto.getEmail());
        user.setComment(dto.getComment());
        user.setMacAddress(dto.getMacAddress());
        user.setStaticIp(dto.getStaticIp());
        user.setActive(dto.getActive());
        user.setProfile(newProfile);
        user.setPassword(dto.getPassword());
        user.setUpdatedAt(LocalDateTime.now());

        PppoeUser updated = repository.save(user);
        log.info("Usuário PPPoE atualizado com sucesso: {} (Mikrotik + Banco)", updated.getId());
        return mapToDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        // Deletar usuário do Mikrotik via API
        apiService.deletePppoeUser(
                user.getMikrotikServer().getIpAddress(),
                user.getMikrotikServer().getApiPort(),
                user.getMikrotikServer().getUsername(),
                user.getMikrotikServer().getPassword(),
                user.getUsername()
        );

        repository.delete(user);
        log.info("Usuário PPPoE deletado: {}", id);
    }

    @Transactional
    public void disable(Long id) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        apiService.disablePppoeUser(
                user.getMikrotikServer().getIpAddress(),
                user.getMikrotikServer().getApiPort(),
                user.getMikrotikServer().getUsername(),
                user.getMikrotikServer().getPassword(),
                user.getUsername()
        );

        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
        log.info("Usuário PPPoE desativado: {}", id);
    }

    @Transactional
    public void enable(Long id) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        apiService.enablePppoeUser(
                user.getMikrotikServer().getIpAddress(),
                user.getMikrotikServer().getApiPort(),
                user.getMikrotikServer().getUsername(),
                user.getMikrotikServer().getPassword(),
                user.getUsername()
        );

        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
        log.info("Usuário PPPoE ativado: {}", id);
    }

    @Transactional
    public SyncResultDTO syncUsersFromMikrotik(Long serverId, Long forceProfileId) {
        SyncResultDTO result = new SyncResultDTO();

        // Buscar servidor
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + serverId));

        // Se forceProfileId foi informado, buscar o perfil
        PppoeProfile forceProfile = null;
        if (forceProfileId != null) {
            forceProfile = profileRepository.findById(forceProfileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + forceProfileId));
            log.info("Sincronização com perfil forçado: {} (ID: {})", forceProfile.getName(), forceProfileId);
        } else {
            log.info("Sincronização mantendo perfis originais do MikroTik");
        }

        log.info("Iniciando sincronização de usuários do servidor {} (ID: {})", server.getName(), serverId);

        try {
            // Buscar usuários do Mikrotik via API (✅ Comentários completos!)
            log.info("Usando MikrotikApiService para sincronização via porta API: {}", server.getApiPort());
            List<MikrotikPppoeUserDTO> mikrotikUsers = apiService.getPppoeUsersStructured(
                    server.getIpAddress(),
                    server.getApiPort(),
                    server.getUsername(),
                    server.getPassword()
            );

            result.setTotalMikrotikUsers(mikrotikUsers.size());

            for (MikrotikPppoeUserDTO mikrotikUser : mikrotikUsers) {
                try {
                    // Verificar se usuário já existe no banco
                    Optional<PppoeUser> existingUserOpt = repository.findByUsernameAndMikrotikServer(
                            mikrotikUser.getUsername(),
                            server
                    );

                    if (existingUserOpt.isPresent()) {
                        // Usuário já existe - ATUALIZAR SENHA do Mikrotik
                        PppoeUser existingUser = existingUserOpt.get();

                        String mikrotikPassword = mikrotikUser.getPassword() != null ?
                                mikrotikUser.getPassword() : "synced123";

                        // Verificar se senha está criptografada (BCrypt) e precisa ser atualizada
                        boolean needsPasswordUpdate = existingUser.getPassword() == null ||
                                                     existingUser.getPassword().isEmpty() ||
                                                     existingUser.getPassword().startsWith("$2a$") ||
                                                     existingUser.getPassword().startsWith("$2b$");

                        if (needsPasswordUpdate || !existingUser.getPassword().equals(mikrotikPassword)) {
                            // Atualizar senha (em texto plano do Mikrotik)
                            existingUser.setPassword(mikrotikPassword);
                            existingUser.setUpdatedAt(LocalDateTime.now());
                            repository.save(existingUser);

                            result.setSkippedUsers(result.getSkippedUsers() + 1);
                            result.getSkippedUsernames().add(mikrotikUser.getUsername() + " (senha atualizada)");
                            log.info("✅ Usuário {} - senha sincronizada do Mikrotik: {}",
                                    mikrotikUser.getUsername(), mikrotikPassword);
                        } else {
                            result.setSkippedUsers(result.getSkippedUsers() + 1);
                            result.getSkippedUsernames().add(mikrotikUser.getUsername());
                            log.debug("Usuário {} já existe com senha correta, pulando", mikrotikUser.getUsername());
                        }
                        continue;
                    }

                    // Criar novo usuário no banco
                    PppoeUser newUser = new PppoeUser();
                    newUser.setCompanyId(server.getCompanyId());
                    newUser.setUsername(mikrotikUser.getUsername());

                    // Se a senha estiver disponível, usar; senão, usar padrão
                    // Armazenar em texto plano (técnicos precisam visualizar)
                    String password = mikrotikUser.getPassword() != null ?
                            mikrotikUser.getPassword() : "synced123";
                    newUser.setPassword(password);

                    // Email padrão baseado no username
                    newUser.setEmail(mikrotikUser.getUsername() + "@synced.local");

                    // Comentário
                    String comment = mikrotikUser.getComment() != null ?
                            mikrotikUser.getComment() : "Sincronizado do Mikrotik";
                    newUser.setComment(comment);

                    // Status ativo
                    newUser.setActive(mikrotikUser.getDisabled() == null || !mikrotikUser.getDisabled());

                    // Determinar qual perfil usar
                    PppoeProfile profileToUse;

                    if (forceProfile != null) {
                        // Se forceProfileId foi informado, usar esse perfil para todos
                        profileToUse = forceProfile;
                    } else {
                        // Buscar perfil pelo nome do MikroTik
                        if (mikrotikUser.getProfile() != null) {
                            Optional<PppoeProfile> foundProfile = profileRepository
                                    .findByNameAndMikrotikServer(mikrotikUser.getProfile(), server);
                            if (foundProfile.isPresent()) {
                                profileToUse = foundProfile.get();
                            } else {
                                // Perfil não encontrado no banco
                                result.setFailedUsers(result.getFailedUsers() + 1);
                                String errorMsg = String.format("Perfil '%s' do usuário '%s' não encontrado no banco. " +
                                        "Sincronize os perfis primeiro ou use forceProfileId.",
                                        mikrotikUser.getProfile(), mikrotikUser.getUsername());
                                result.getErrorMessages().add(errorMsg);
                                log.warn(errorMsg);
                                continue;
                            }
                        } else {
                            // Usuário do MikroTik sem perfil
                            result.setFailedUsers(result.getFailedUsers() + 1);
                            String errorMsg = String.format("Usuário '%s' não possui perfil no MikroTik. " +
                                    "Use forceProfileId para definir um perfil padrão.",
                                    mikrotikUser.getUsername());
                            result.getErrorMessages().add(errorMsg);
                            log.warn(errorMsg);
                            continue;
                        }
                    }

                    newUser.setProfile(profileToUse);
                    newUser.setMikrotikServer(server);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());

                    repository.save(newUser);

                    result.setSyncedUsers(result.getSyncedUsers() + 1);
                    result.getSyncedUsernames().add(mikrotikUser.getUsername());
                    log.info("Usuário {} sincronizado com perfil {}",
                            mikrotikUser.getUsername(), profileToUse.getName());

                } catch (Exception e) {
                    result.setFailedUsers(result.getFailedUsers() + 1);
                    String errorMsg = String.format("Erro ao sincronizar %s: %s",
                            mikrotikUser.getUsername(), e.getMessage());
                    result.getErrorMessages().add(errorMsg);
                    log.error(errorMsg, e);
                }
            }

            log.info("Sincronização concluída. Total: {}, Sincronizados: {}, Pulados: {}, Falhas: {}",
                    result.getTotalMikrotikUsers(), result.getSyncedUsers(),
                    result.getSkippedUsers(), result.getFailedUsers());

        } catch (Exception e) {
            log.error("Erro ao buscar usuários do Mikrotik: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro ao buscar usuários do Mikrotik: " + e.getMessage());
        }

        return result;
    }

    private PppoeUserDTO mapToDTO(PppoeUser user) {
        PppoeUserDTO dto = new PppoeUserDTO();
        dto.setId(user.getId());
        dto.setCompanyId(user.getCompanyId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword()); // Retornar senha (técnicos precisam visualizar)
        dto.setEmail(user.getEmail());
        dto.setComment(user.getComment());
        dto.setMacAddress(user.getMacAddress());
        dto.setStaticIp(user.getStaticIp());
        dto.setStatus(user.getStatus());
        dto.setActive(user.getActive());
        dto.setLastConnectionAt(user.getLastConnectionAt());
        dto.setProfileId(user.getProfile().getId());
        dto.setMikrotikServerId(user.getMikrotikServer().getId());
        return dto;
    }
}
