package br.com.mikrotik.service;

import br.com.mikrotik.dto.MikrotikPppoeUserDTO;
import br.com.mikrotik.dto.PppoeUserDTO;
import br.com.mikrotik.dto.SyncResultDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.model.MikrotikServer;
import br.com.mikrotik.model.PppoeProfile;
import br.com.mikrotik.model.PppoeUser;
import br.com.mikrotik.repository.MikrotikServerRepository;
import br.com.mikrotik.repository.PppoeProfileRepository;
import br.com.mikrotik.repository.PppoeUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final MikrotikSshService sshService;
    private final PasswordEncoder passwordEncoder;

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
        sshService.createPppoeUser(
                server.getIpAddress(),
                server.getPort(),
                server.getUsername(),
                server.getPassword(),
                dto.getUsername(),
                dto.getPassword(),
                profile.getName()
        );

        PppoeUser user = new PppoeUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setComment(dto.getComment());
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

    @Transactional
    public PppoeUserDTO update(Long id, PppoeUserDTO dto) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        PppoeProfile profile = profileRepository.findById(dto.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado"));

        user.setEmail(dto.getEmail());
        user.setComment(dto.getComment());
        user.setActive(dto.getActive());
        user.setProfile(profile);
        user.setUpdatedAt(LocalDateTime.now());

        PppoeUser updated = repository.save(user);
        log.info("Usuário PPPoE atualizado: {}", updated.getId());
        return mapToDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        // Deletar usuário do Mikrotik
        sshService.deletePppoeUser(
                user.getMikrotikServer().getIpAddress(),
                user.getMikrotikServer().getPort(),
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

        sshService.disablePppoeUser(
                user.getMikrotikServer().getIpAddress(),
                user.getMikrotikServer().getPort(),
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

        sshService.enablePppoeUser(
                user.getMikrotikServer().getIpAddress(),
                user.getMikrotikServer().getPort(),
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
            // Buscar usuários do Mikrotik
            List<MikrotikPppoeUserDTO> mikrotikUsers = sshService.getPppoeUsersStructured(
                    server.getIpAddress(),
                    server.getPort(),
                    server.getUsername(),
                    server.getPassword()
            );

            result.setTotalMikrotikUsers(mikrotikUsers.size());

            for (MikrotikPppoeUserDTO mikrotikUser : mikrotikUsers) {
                try {
                    // Verificar se usuário já existe no banco
                    Optional<PppoeUser> existingUser = repository.findByUsernameAndMikrotikServer(
                            mikrotikUser.getUsername(),
                            server
                    );

                    if (existingUser.isPresent()) {
                        // Usuário já existe, pular
                        result.setSkippedUsers(result.getSkippedUsers() + 1);
                        result.getSkippedUsernames().add(mikrotikUser.getUsername());
                        log.debug("Usuário {} já existe no banco, pulando", mikrotikUser.getUsername());
                        continue;
                    }

                    // Criar novo usuário no banco
                    PppoeUser newUser = new PppoeUser();
                    newUser.setUsername(mikrotikUser.getUsername());

                    // Se a senha estiver disponível, usar; senão, usar padrão
                    String password = mikrotikUser.getPassword() != null ?
                            mikrotikUser.getPassword() : "synced123";
                    newUser.setPassword(passwordEncoder.encode(password));

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
        return new PppoeUserDTO(
                user.getId(),
                user.getUsername(),
                "", // Não retornar password por segurança
                user.getEmail(),
                user.getComment(),
                user.getActive(),
                user.getProfile().getId(),
                user.getMikrotikServer().getId()
        );
    }
}
