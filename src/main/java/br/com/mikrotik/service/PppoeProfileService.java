package br.com.mikrotik.service;

import br.com.mikrotik.dto.PppoeProfileDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.model.MikrotikServer;
import br.com.mikrotik.model.PppoeProfile;
import br.com.mikrotik.repository.MikrotikServerRepository;
import br.com.mikrotik.repository.PppoeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PppoeProfileService {

    private final PppoeProfileRepository repository;
    private final MikrotikServerRepository serverRepository;

    @Transactional
    public PppoeProfileDTO create(PppoeProfileDTO dto) {
        MikrotikServer server = serverRepository.findById(dto.getMikrotikServerId())
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));

        PppoeProfile profile = new PppoeProfile();
        profile.setName(dto.getName());
        profile.setDescription(dto.getDescription());
        profile.setMaxBitrateDl(dto.getMaxBitrateDl());
        profile.setMaxBitrateUl(dto.getMaxBitrateUl());
        profile.setSessionTimeout(dto.getSessionTimeout());
        profile.setActive(dto.getActive() != null ? dto.getActive() : true);
        profile.setMikrotikServer(server);
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        PppoeProfile saved = repository.save(profile);
        log.info("Perfil PPPoE criado: {}", saved.getId());
        return mapToDTO(saved);
    }

    public PppoeProfileDTO getById(Long id) {
        PppoeProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + id));
        return mapToDTO(profile);
    }

    public List<PppoeProfileDTO> getByServer(Long serverId) {
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));
        return repository.findByMikrotikServer(server).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<PppoeProfileDTO> getAll() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public PppoeProfileDTO update(Long id, PppoeProfileDTO dto) {
        PppoeProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + id));

        profile.setName(dto.getName());
        profile.setDescription(dto.getDescription());
        profile.setMaxBitrateDl(dto.getMaxBitrateDl());
        profile.setMaxBitrateUl(dto.getMaxBitrateUl());
        profile.setSessionTimeout(dto.getSessionTimeout());
        profile.setActive(dto.getActive());
        profile.setUpdatedAt(LocalDateTime.now());

        PppoeProfile updated = repository.save(profile);
        log.info("Perfil PPPoE atualizado: {}", updated.getId());
        return mapToDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        PppoeProfile profile = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado: " + id));
        repository.delete(profile);
        log.info("Perfil PPPoE deletado: {}", id);
    }

    private PppoeProfileDTO mapToDTO(PppoeProfile profile) {
        return new PppoeProfileDTO(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getMaxBitrateDl(),
                profile.getMaxBitrateUl(),
                profile.getSessionTimeout(),
                profile.getActive(),
                profile.getMikrotikServer().getId()
        );
    }
}
