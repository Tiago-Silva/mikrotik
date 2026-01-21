package br.com.mikrotik.service;

import br.com.mikrotik.dto.PppoeUserDTO;
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

    public List<PppoeUserDTO> getAll() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public PppoeUserDTO update(Long id, PppoeUserDTO dto) {
        PppoeUser user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + id));

        MikrotikServer server = user.getMikrotikServer();
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
