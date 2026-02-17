package br.com.mikrotik.features.network.pppoe.service;

import br.com.mikrotik.features.network.pppoe.dto.PppoeConnectionDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.features.network.pppoe.model.PppoeConnection;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeConnectionRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    public Page<PppoeConnectionDTO> getByUser(Long userId, Pageable pageable) {
        PppoeUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return repository.findByUser(user, pageable)
                .map(this::mapToDTO);
    }

    public Page<PppoeConnectionDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDTO);
    }

    public Long countActive() {
        return repository.findAll().stream()
                .filter(PppoeConnection::getActive)
                .count();
    }

    public Page<PppoeConnectionDTO> getActiveByServer(Long serverId, Pageable pageable) {
        MikrotikServer server = serverRepository.findById(serverId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado"));
        return repository.findByMikrotikServerAndActive(server, true, pageable)
                .map(this::mapToDTO);
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
