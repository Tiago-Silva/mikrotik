package br.com.mikrotik.service;

import br.com.mikrotik.dto.PppoeConnectionDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.model.MikrotikServer;
import br.com.mikrotik.model.PppoeConnection;
import br.com.mikrotik.model.PppoeUser;
import br.com.mikrotik.repository.MikrotikServerRepository;
import br.com.mikrotik.repository.PppoeConnectionRepository;
import br.com.mikrotik.repository.PppoeUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PppoeConnectionService {

    private final PppoeConnectionRepository repository;
    private final MikrotikServerRepository serverRepository;
    private final PppoeUserRepository userRepository;

    public PppoeConnectionDTO getById(Long id) {
        PppoeConnection connection = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conexão não encontrada: " + id));
        return mapToDTO(connection);
    }

    public Page<PppoeConnectionDTO> getByServer(Long serverId, Pageable pageable) {
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));
        return repository.findByMikrotikServer(server, pageable)
                .map(this::mapToDTO);
    }

    public List<PppoeConnectionDTO> getByUser(Long userId) {
        PppoeUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return repository.findByUser(user).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<PppoeConnectionDTO> getAll() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public Long countActive() {
        return repository.findAll().stream()
                .filter(PppoeConnection::getActive)
                .count();
    }

    public List<PppoeConnectionDTO> getActiveByServer(Long serverId) {
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));
        return repository.findByMikrotikServerAndActive(server, true).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public PppoeConnectionDTO recordConnection(Long userId, String ipAddress, String callingStationId, Long serverId) {
        PppoeUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));

        PppoeConnection connection = new PppoeConnection();
        connection.setUser(user);
        connection.setIpAddress(ipAddress);
        connection.setCallingStationId(callingStationId);
        connection.setConnectedAt(LocalDateTime.now());
        connection.setActive(true);
        connection.setMikrotikServer(server);
        connection.setCreatedAt(LocalDateTime.now());
        connection.setUpdatedAt(LocalDateTime.now());

        PppoeConnection saved = repository.save(connection);
        log.info("Conexão registrada para usuário: {}", user.getUsername());
        return mapToDTO(saved);
    }

    @Transactional
    public void recordDisconnection(Long connectionId, Long bytesUp, Long bytesDown) {
        PppoeConnection connection = repository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Conexão não encontrada"));

        connection.setDisconnectedAt(LocalDateTime.now());
        connection.setActive(false);
        connection.setBytesUp(bytesUp);
        connection.setBytesDown(bytesDown);
        connection.setUpdatedAt(LocalDateTime.now());

        repository.save(connection);
        log.info("Conexão finalizada para usuário: {}", connection.getUser().getUsername());
    }

    private PppoeConnectionDTO mapToDTO(PppoeConnection connection) {
        return new PppoeConnectionDTO(
                connection.getId(),
                connection.getUser().getId(),
                connection.getUser().getUsername(),
                connection.getIpAddress(),
                connection.getCallingStationId(),
                connection.getConnectedAt(),
                connection.getDisconnectedAt(),
                connection.getBytesUp(),
                connection.getBytesDown(),
                connection.getActive(),
                connection.getMikrotikServer().getId()
        );
    }
}
