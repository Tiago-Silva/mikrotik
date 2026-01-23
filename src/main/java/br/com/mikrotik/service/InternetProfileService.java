package br.com.mikrotik.service;

import br.com.mikrotik.dto.InternetProfileDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.InternetProfile;
import br.com.mikrotik.repository.InternetProfileRepository;
import br.com.mikrotik.repository.IpPoolRepository;
import br.com.mikrotik.repository.MikrotikServerRepository;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternetProfileService {

    private final InternetProfileRepository internetProfileRepository;
    private final MikrotikServerRepository mikrotikServerRepository;
    private final IpPoolRepository ipPoolRepository;

    /**
     * Criar novo perfil de internet
     */
    @Transactional
    public InternetProfileDTO create(InternetProfileDTO dto) {
        log.info("Criando novo perfil de internet: {}", dto.getName());

        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }

        // Validar se servidor existe
        if (!mikrotikServerRepository.existsById(dto.getMikrotikServerId())) {
            throw new ValidationException("Servidor Mikrotik não encontrado com ID: " + dto.getMikrotikServerId());
        }

        // Validar pool de IP se fornecido
        if (dto.getRemoteAddressPoolId() != null) {
            if (!ipPoolRepository.existsById(dto.getRemoteAddressPoolId())) {
                throw new ValidationException("Pool de IP não encontrado com ID: " + dto.getRemoteAddressPoolId());
            }
        }

        // Verificar se nome já existe na empresa
        if (internetProfileRepository.existsByNameAndCompanyId(dto.getName(), companyId)) {
            throw new ValidationException("Já existe um perfil com este nome nesta empresa");
        }

        dto.setCompanyId(companyId);
        InternetProfile profile = dto.toEntity();
        profile = internetProfileRepository.save(profile);

        log.info("Perfil de internet criado com sucesso: ID={}", profile.getId());
        return InternetProfileDTO.fromEntity(profile);
    }

    /**
     * Buscar perfil por ID
     */
    @Transactional(readOnly = true)
    public InternetProfileDTO findById(Long id) {
        log.info("Buscando perfil de internet por ID: {}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        InternetProfile profile = internetProfileRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de internet não encontrado com ID: " + id));

        return InternetProfileDTO.fromEntity(profile);
    }

    /**
     * Listar perfis por empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InternetProfileDTO> findAll(Pageable pageable) {
        log.info("Listando perfis de internet");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<InternetProfile> profiles = internetProfileRepository.findByCompanyId(companyId, pageable);
        return profiles.map(InternetProfileDTO::fromEntity);
    }

    /**
     * Listar perfis por servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InternetProfileDTO> findByServer(Long serverId, Pageable pageable) {
        log.info("Listando perfis de internet do servidor: {}", serverId);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<InternetProfile> profiles = internetProfileRepository.findByCompanyIdAndMikrotikServerId(
                companyId, serverId, pageable);
        return profiles.map(InternetProfileDTO::fromEntity);
    }

    /**
     * Listar perfis ativos (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InternetProfileDTO> findByActive(Boolean active, Pageable pageable) {
        log.info("Listando perfis de internet ativos: {}", active);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<InternetProfile> profiles = internetProfileRepository.findByCompanyIdAndActive(companyId, active, pageable);
        return profiles.map(InternetProfileDTO::fromEntity);
    }

    /**
     * Listar perfis ativos por servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<InternetProfileDTO> findByServerAndActive(Long serverId, Boolean active, Pageable pageable) {
        log.info("Listando perfis de internet ativos do servidor: {}", serverId);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<InternetProfile> profiles = internetProfileRepository.findByCompanyIdAndMikrotikServerIdAndActive(
                companyId, serverId, active, pageable);
        return profiles.map(InternetProfileDTO::fromEntity);
    }

    /**
     * Listar todos os perfis da empresa (sem paginação)
     */
    @Transactional(readOnly = true)
    public List<InternetProfileDTO> findAllByCompany() {
        log.info("Listando todos os perfis da empresa");

        Long companyId = CompanyContextHolder.getCompanyId();
        List<InternetProfile> profiles = internetProfileRepository.findByCompanyId(companyId);
        return profiles.stream()
                .map(InternetProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Listar perfis ativos da empresa (sem paginação)
     */
    @Transactional(readOnly = true)
    public List<InternetProfileDTO> findActiveByCompany() {
        log.info("Listando perfis ativos da empresa");

        Long companyId = CompanyContextHolder.getCompanyId();
        List<InternetProfile> profiles = internetProfileRepository.findByCompanyIdAndActive(companyId, true);
        return profiles.stream()
                .map(InternetProfileDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<InternetProfileDTO> findByFilters(String name, Long serverId, Boolean active, Pageable pageable) {
        log.info("Buscando perfis com filtros - nome: {}, servidor: {}, ativo: {}", name, serverId, active);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<InternetProfile> profiles = internetProfileRepository.findByFilters(companyId, name, serverId, active, pageable);
        return profiles.map(InternetProfileDTO::fromEntity);
    }

    /**
     * Atualizar perfil
     */
    @Transactional
    public InternetProfileDTO update(Long id, InternetProfileDTO dto) {
        log.info("Atualizando perfil de internet: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        InternetProfile existing = internetProfileRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de internet não encontrado com ID: " + id));

        // Verificar se nome foi alterado e já existe
        if (!existing.getName().equals(dto.getName())) {
            if (internetProfileRepository.existsByNameAndCompanyId(dto.getName(), companyId)) {
                throw new ValidationException("Já existe um perfil com este nome nesta empresa");
            }
        }

        // Validar pool de IP se fornecido
        if (dto.getRemoteAddressPoolId() != null) {
            if (!ipPoolRepository.existsById(dto.getRemoteAddressPoolId())) {
                throw new ValidationException("Pool de IP não encontrado com ID: " + dto.getRemoteAddressPoolId());
            }
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setDownloadKbit(dto.getDownloadKbit());
        existing.setUploadKbit(dto.getUploadKbit());
        existing.setSessionTimeout(dto.getSessionTimeout());
        existing.setRemoteAddressPoolId(dto.getRemoteAddressPoolId());
        existing.setActive(dto.getActive());

        existing = internetProfileRepository.save(existing);

        log.info("Perfil de internet atualizado com sucesso: ID={}", id);
        return InternetProfileDTO.fromEntity(existing);
    }

    /**
     * Ativar/Desativar perfil
     */
    @Transactional
    public InternetProfileDTO toggleActive(Long id, Boolean active) {
        log.info("Alterando status do perfil de internet: ID={}, ativo={}", id, active);

        Long companyId = CompanyContextHolder.getCompanyId();
        InternetProfile profile = internetProfileRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de internet não encontrado com ID: " + id));

        profile.setActive(active);
        profile = internetProfileRepository.save(profile);

        log.info("Status do perfil alterado com sucesso: ID={}", id);
        return InternetProfileDTO.fromEntity(profile);
    }

    /**
     * Deletar perfil
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando perfil de internet: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        InternetProfile profile = internetProfileRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de internet não encontrado com ID: " + id));

        internetProfileRepository.delete(profile);
        log.info("Perfil de internet deletado com sucesso: ID={}", id);
    }

    /**
     * Contar perfis por empresa
     */
    @Transactional(readOnly = true)
    public long countByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return internetProfileRepository.countByCompanyId(companyId);
    }

    /**
     * Contar perfis ativos por empresa
     */
    @Transactional(readOnly = true)
    public long countActiveByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return internetProfileRepository.countByCompanyIdAndActive(companyId, true);
    }
}
