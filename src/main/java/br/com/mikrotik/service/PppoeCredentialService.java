package br.com.mikrotik.service;

import br.com.mikrotik.dto.PppoeCredentialDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.PppoeCredential;
import br.com.mikrotik.repository.MikrotikServerRepository;
import br.com.mikrotik.repository.PppoeCredentialRepository;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PppoeCredentialService {

    private final PppoeCredentialRepository pppoeCredentialRepository;
    private final MikrotikServerRepository mikrotikServerRepository;

    /**
     * Criar nova credencial PPPoE
     */
    @Transactional
    public PppoeCredentialDTO create(PppoeCredentialDTO dto) {
        log.info("Criando nova credencial PPPoE: {}", dto.getUsername());

        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }

        // Validar se servidor existe
        if (!mikrotikServerRepository.existsById(dto.getMikrotikServerId())) {
            throw new ValidationException("Servidor Mikrotik não encontrado com ID: " + dto.getMikrotikServerId());
        }

        // Verificar se username já existe no servidor
        if (pppoeCredentialRepository.existsByUsernameAndMikrotikServerId(dto.getUsername(), dto.getMikrotikServerId())) {
            throw new ValidationException("Já existe uma credencial com este username neste servidor");
        }

        dto.setCompanyId(companyId);
        PppoeCredential credential = dto.toEntity();
        credential = pppoeCredentialRepository.save(credential);

        log.info("Credencial PPPoE criada com sucesso: ID={}", credential.getId());
        return PppoeCredentialDTO.fromEntity(credential);
    }

    /**
     * Buscar credencial por ID
     */
    @Transactional(readOnly = true)
    public PppoeCredentialDTO findById(Long id) {
        log.info("Buscando credencial PPPoE por ID: {}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        PppoeCredential credential = pppoeCredentialRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial PPPoE não encontrada com ID: " + id));

        return PppoeCredentialDTO.fromEntity(credential);
    }

    /**
     * Listar credenciais por empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<PppoeCredentialDTO> findAll(Pageable pageable) {
        log.info("Listando credenciais PPPoE");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<PppoeCredential> credentials = pppoeCredentialRepository.findByCompanyId(companyId, pageable);
        return credentials.map(PppoeCredentialDTO::fromEntity);
    }

    /**
     * Listar credenciais por servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<PppoeCredentialDTO> findByServer(Long serverId, Pageable pageable) {
        log.info("Listando credenciais PPPoE do servidor: {}", serverId);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<PppoeCredential> credentials = pppoeCredentialRepository.findByCompanyIdAndMikrotikServerId(
                companyId, serverId, pageable);
        return credentials.map(PppoeCredentialDTO::fromEntity);
    }

    /**
     * Listar credenciais por status (paginado)
     */
    @Transactional(readOnly = true)
    public Page<PppoeCredentialDTO> findByStatus(PppoeCredential.CredentialStatus status, Pageable pageable) {
        log.info("Listando credenciais PPPoE por status: {}", status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<PppoeCredential> credentials = pppoeCredentialRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return credentials.map(PppoeCredentialDTO::fromEntity);
    }

    /**
     * Listar credenciais não vinculadas a contratos (paginado)
     */
    @Transactional(readOnly = true)
    public Page<PppoeCredentialDTO> findUnassigned(Pageable pageable) {
        log.info("Listando credenciais não vinculadas a contratos");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<PppoeCredential> credentials = pppoeCredentialRepository.findUnassignedCredentials(companyId, pageable);
        return credentials.map(PppoeCredentialDTO::fromEntity);
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<PppoeCredentialDTO> findByFilters(String username, Long serverId,
                                                   PppoeCredential.CredentialStatus status, Pageable pageable) {
        log.info("Buscando credenciais com filtros - username: {}, servidor: {}, status: {}", username, serverId, status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<PppoeCredential> credentials = pppoeCredentialRepository.findByFilters(
                companyId, username, serverId, status, pageable);
        return credentials.map(PppoeCredentialDTO::fromEntity);
    }

    /**
     * Atualizar credencial
     */
    @Transactional
    public PppoeCredentialDTO update(Long id, PppoeCredentialDTO dto) {
        log.info("Atualizando credencial PPPoE: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        PppoeCredential existing = pppoeCredentialRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial PPPoE não encontrada com ID: " + id));

        // Verificar se username foi alterado e já existe
        if (!existing.getUsername().equals(dto.getUsername())) {
            if (pppoeCredentialRepository.existsByUsernameAndMikrotikServerId(dto.getUsername(), dto.getMikrotikServerId())) {
                throw new ValidationException("Já existe uma credencial com este username neste servidor");
            }
        }

        existing.setUsername(dto.getUsername());
        existing.setPassword(dto.getPassword());
        existing.setMacAddress(dto.getMacAddress());
        existing.setStaticIp(dto.getStaticIp());
        existing.setStatus(dto.getStatus());

        existing = pppoeCredentialRepository.save(existing);

        log.info("Credencial PPPoE atualizada com sucesso: ID={}", id);
        return PppoeCredentialDTO.fromEntity(existing);
    }

    /**
     * Alterar status da credencial
     */
    @Transactional
    public PppoeCredentialDTO updateStatus(Long id, PppoeCredential.CredentialStatus status) {
        log.info("Alterando status da credencial PPPoE: ID={}, status={}", id, status);

        Long companyId = CompanyContextHolder.getCompanyId();
        PppoeCredential credential = pppoeCredentialRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial PPPoE não encontrada com ID: " + id));

        credential.setStatus(status);

        // Atualizar última conexão se status for ONLINE
        if (status == PppoeCredential.CredentialStatus.ONLINE) {
            credential.setLastConnectionAt(LocalDateTime.now());
        }

        credential = pppoeCredentialRepository.save(credential);

        log.info("Status da credencial alterado com sucesso: ID={}", id);
        return PppoeCredentialDTO.fromEntity(credential);
    }

    /**
     * Deletar credencial
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando credencial PPPoE: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        PppoeCredential credential = pppoeCredentialRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Credencial PPPoE não encontrada com ID: " + id));

        pppoeCredentialRepository.delete(credential);
        log.info("Credencial PPPoE deletada com sucesso: ID={}", id);
    }

    /**
     * Contar credenciais por empresa
     */
    @Transactional(readOnly = true)
    public long countByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return pppoeCredentialRepository.countByCompanyId(companyId);
    }

    /**
     * Contar credenciais por status
     */
    @Transactional(readOnly = true)
    public long countByStatus(PppoeCredential.CredentialStatus status) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return pppoeCredentialRepository.countByCompanyIdAndStatus(companyId, status);
    }
}
