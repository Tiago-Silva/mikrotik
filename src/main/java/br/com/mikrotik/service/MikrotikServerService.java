package br.com.mikrotik.service;

import br.com.mikrotik.dto.MikrotikServerDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.model.Company;
import br.com.mikrotik.model.MikrotikServer;
import br.com.mikrotik.repository.CompanyRepository;
import br.com.mikrotik.repository.MikrotikServerRepository;
import br.com.mikrotik.util.CompanyContextHolder;
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
public class MikrotikServerService {

    private final MikrotikServerRepository repository;
    private final MikrotikApiService apiService; // Usar API ao invés de SSH
    private final CompanyRepository companyRepository;

    @Transactional
    public MikrotikServerDTO create(MikrotikServerDTO dto) {
        log.info("Criando novo servidor Mikrotik: {}", dto.getName());

        // Testar conexão via API antes de salvar
        apiService.testConnection(dto.getIpAddress(), dto.getApiPort(), dto.getUsername(), dto.getPassword());

        MikrotikServer server = new MikrotikServer();
        server.setName(dto.getName());
        server.setIpAddress(dto.getIpAddress());
        server.setPort(dto.getPort());
        server.setApiPort(dto.getApiPort() != null ? dto.getApiPort() : 8728);
        server.setUsername(dto.getUsername());
        server.setPassword(dto.getPassword());
        server.setDescription(dto.getDescription());
        server.setActive(dto.getActive() != null ? dto.getActive() : true);
        server.setCreatedAt(LocalDateTime.now());
        server.setUpdatedAt(LocalDateTime.now());

        // Multi-tenant: Associar à company do contexto (se disponível)
        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId != null) {
            server.setCompanyId(companyId);
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + companyId));
            server.setCompany(company);
            log.info("Servidor associado à empresa: {}", companyId);
        }

        MikrotikServer saved = repository.save(server);
        log.info("Servidor Mikrotik criado: {}", saved.getId());
        return mapToDTO(saved);
    }

    public MikrotikServerDTO getById(Long id) {
        MikrotikServer server = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + id));
        return mapToDTO(server);
    }

    public Page<MikrotikServerDTO> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::mapToDTO);
    }

    /**
     * Lista servidores filtrados por empresa (multi-tenant)
     */
    @Transactional(readOnly = true)
    public Page<MikrotikServerDTO> findByCompanyId(Long companyId, Pageable pageable) {
        log.info("Buscando servidores da empresa: {}", companyId);
        return repository.findByCompanyId(companyId, pageable)
                .map(this::mapToDTO);
    }

    /**
     * Lista servidores ativos filtrados por empresa
     */
    @Transactional(readOnly = true)
    public Page<MikrotikServerDTO> findByCompanyIdAndActive(Long companyId, Boolean active, Pageable pageable) {
        log.info("Buscando servidores da empresa {} com active={}", companyId, active);
        return repository.findByCompanyIdAndActive(companyId, active, pageable)
                .map(this::mapToDTO);
    }

    @Transactional
    public MikrotikServerDTO update(Long id, MikrotikServerDTO dto) {
        MikrotikServer server = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor Mikrotik não encontrado: " + id));

        // Se credenciais mudaram, testar nova conexão via API
        if (!server.getIpAddress().equals(dto.getIpAddress()) ||
            !server.getUsername().equals(dto.getUsername()) ||
            !server.getPassword().equals(dto.getPassword())) {
            apiService.testConnection(dto.getIpAddress(), dto.getApiPort(), dto.getUsername(), dto.getPassword());
        }

        server.setName(dto.getName());
        server.setIpAddress(dto.getIpAddress());
        server.setPort(dto.getPort());
        server.setApiPort(dto.getApiPort() != null ? dto.getApiPort() : 8728);
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
        return apiService.testConnection(server.getIpAddress(), server.getApiPort(), server.getUsername(), server.getPassword());
    }

    private MikrotikServerDTO mapToDTO(MikrotikServer server) {
        MikrotikServerDTO dto = new MikrotikServerDTO();
        dto.setId(server.getId());
        dto.setCompanyId(server.getCompanyId());
        dto.setName(server.getName());
        dto.setIpAddress(server.getIpAddress());
        dto.setPort(server.getPort());
        dto.setApiPort(server.getApiPort());
        dto.setUsername(server.getUsername());
        dto.setPassword(server.getPassword());
        dto.setDescription(server.getDescription());
        dto.setActive(server.getActive());
        dto.setLastSyncAt(server.getLastSyncAt());
        dto.setSyncStatus(server.getSyncStatus());
        dto.setCreatedAt(server.getCreatedAt());
        dto.setUpdatedAt(server.getUpdatedAt());
        return dto;
    }
}
