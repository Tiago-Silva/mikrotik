package br.com.mikrotik.service;

import br.com.mikrotik.dto.MikrotikServerDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.model.MikrotikServer;
import br.com.mikrotik.repository.MikrotikServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MikrotikServerService {

    private final MikrotikServerRepository repository;
    private final MikrotikSshService sshService;

    @Transactional
    public MikrotikServerDTO create(MikrotikServerDTO dto) {
        // Testar conexão antes de salvar
        sshService.testConnection(dto.getIpAddress(), dto.getPort(), dto.getUsername(), dto.getPassword());

        MikrotikServer server = new MikrotikServer();
        server.setName(dto.getName());
        server.setIpAddress(dto.getIpAddress());
        server.setPort(dto.getPort());
        server.setUsername(dto.getUsername());
        server.setPassword(dto.getPassword());
        server.setDescription(dto.getDescription());
        server.setActive(dto.getActive() != null ? dto.getActive() : true);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());

        MikrotikServer saved = repository.save(server);
        log.info("Servidor Mikrotik criado: {}", saved.getId());
        return mapToDTO(saved);
    }

    public MikrotikServerDTO getById(Long id) {
        MikrotikServer server = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + id));
        return mapToDTO(server);
    }

    public List<MikrotikServerDTO> getAll() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public MikrotikServerDTO update(Long id, MikrotikServerDTO dto) {
        MikrotikServer server = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + id));

        // Se credenciais mudaram, testar nova conexão
        if (!server.getIpAddress().equals(dto.getIpAddress()) ||
            !server.getUsername().equals(dto.getUsername()) ||
            !server.getPassword().equals(dto.getPassword())) {
            sshService.testConnection(dto.getIpAddress(), dto.getPort(), dto.getUsername(), dto.getPassword());
        }

        server.setName(dto.getName());
        server.setIpAddress(dto.getIpAddress());
        server.setPort(dto.getPort());
        server.setUsername(dto.getUsername());
        server.setPassword(dto.getPassword());
        server.setDescription(dto.getDescription());
        server.setActive(dto.getActive());
        server.setUpdatedAt(LocalDateTime.now());

        MikrotikServer updated = repository.save(server);
        log.info("Servidor Mikrotik atualizado: {}", updated.getId());
        return mapToDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        MikrotikServer server = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + id));
        repository.delete(server);
        log.info("Servidor Mikrotik deletado: {}", id);
    }

    public boolean testConnection(Long id) {
        MikrotikServer server = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + id));
        return sshService.testConnection(server.getIpAddress(), server.getPort(), server.getUsername(), server.getPassword());
    }

    private MikrotikServerDTO mapToDTO(MikrotikServer server) {
        return new MikrotikServerDTO(
                server.getId(),
                server.getName(),
                server.getIpAddress(),
                server.getPort(),
                server.getUsername(),
                server.getPassword(),
                server.getDescription(),
                server.getActive()
        );
    }
}
