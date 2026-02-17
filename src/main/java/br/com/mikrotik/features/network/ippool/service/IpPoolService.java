package br.com.mikrotik.features.network.ippool.service;

import br.com.mikrotik.features.network.ippool.dto.IpPoolDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.infrastructure.exception.ValidationException;
import br.com.mikrotik.features.network.ippool.model.IpPool;
import br.com.mikrotik.features.network.ippool.repository.IpPoolRepository;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpPoolService {

    private final IpPoolRepository ipPoolRepository;
    private final MikrotikServerRepository mikrotikServerRepository;

    /**
     * Criar novo pool de IP
     */
    @Transactional
    public IpPoolDTO create(IpPoolDTO dto) {
        log.info("Criando novo pool de IP: {}", dto.getName());

        // Validar se servidor existe
        if (!mikrotikServerRepository.existsById(dto.getMikrotikServerId())) {
            throw new ValidationException("Servidor Mikrotik não encontrado com ID: " + dto.getMikrotikServerId());
        }

        // Verificar se nome já existe no servidor
        if (ipPoolRepository.existsByNameAndMikrotikServerId(dto.getName(), dto.getMikrotikServerId())) {
            throw new ValidationException("Já existe um pool com este nome neste servidor");
        }

        IpPool ipPool = dto.toEntity();
        ipPool = ipPoolRepository.save(ipPool);

        log.info("Pool de IP criado com sucesso: ID={}", ipPool.getId());
        return IpPoolDTO.fromEntity(ipPool);
    }

    /**
     * Buscar pool por ID
     */
    @Transactional(readOnly = true)
    public IpPoolDTO findById(Long id) {
        log.info("Buscando pool de IP por ID: {}", id);

        IpPool ipPool = ipPoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool de IP não encontrado com ID: " + id));

        return IpPoolDTO.fromEntity(ipPool);
    }

    /**
     * Listar pools por servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<IpPoolDTO> findByServer(Long serverId, Pageable pageable) {
        log.info("Listando pools de IP do servidor: {}", serverId);

        Page<IpPool> pools = ipPoolRepository.findByMikrotikServerId(serverId, pageable);
        return pools.map(IpPoolDTO::fromEntity);
    }

    /**
     * Listar pools ativos por servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<IpPoolDTO> findByServerAndActive(Long serverId, Boolean active, Pageable pageable) {
        log.info("Listando pools de IP ativos do servidor: {}", serverId);

        Page<IpPool> pools = ipPoolRepository.findByMikrotikServerIdAndActive(serverId, active, pageable);
        return pools.map(IpPoolDTO::fromEntity);
    }

    /**
     * Listar todos os pools de um servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<IpPoolDTO> findAllByServer(Long serverId, Pageable pageable) {
        log.info("Listando todos os pools do servidor: {}", serverId);

        Page<IpPool> pools = ipPoolRepository.findByMikrotikServerId(serverId, pageable);
        return pools.map(IpPoolDTO::fromEntity);
    }

    /**
     * Listar pools ativos de um servidor (paginado)
     */
    @Transactional(readOnly = true)
    public Page<IpPoolDTO> findActiveByServer(Long serverId, Pageable pageable) {
        log.info("Listando pools ativos do servidor: {}", serverId);

        Page<IpPool> pools = ipPoolRepository.findByMikrotikServerIdAndActive(serverId, true, pageable);
        return pools.map(IpPoolDTO::fromEntity);
    }

    /**
     * Atualizar pool
     */
    @Transactional
    public IpPoolDTO update(Long id, IpPoolDTO dto) {
        log.info("Atualizando pool de IP: ID={}", id);

        IpPool existing = ipPoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool de IP não encontrado com ID: " + id));

        // Verificar se nome foi alterado e já existe
        if (!existing.getName().equals(dto.getName())) {
            if (ipPoolRepository.existsByNameAndMikrotikServerId(dto.getName(), dto.getMikrotikServerId())) {
                throw new ValidationException("Já existe um pool com este nome neste servidor");
            }
        }

        existing.setName(dto.getName());
        existing.setCidr(dto.getCidr());
        existing.setActive(dto.getActive());

        existing = ipPoolRepository.save(existing);

        log.info("Pool de IP atualizado com sucesso: ID={}", id);
        return IpPoolDTO.fromEntity(existing);
    }

    /**
     * Ativar/Desativar pool
     */
    @Transactional
    public IpPoolDTO toggleActive(Long id, Boolean active) {
        log.info("Alterando status do pool de IP: ID={}, ativo={}", id, active);

        IpPool ipPool = ipPoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool de IP não encontrado com ID: " + id));

        ipPool.setActive(active);
        ipPool = ipPoolRepository.save(ipPool);

        log.info("Status do pool alterado com sucesso: ID={}", id);
        return IpPoolDTO.fromEntity(ipPool);
    }

    /**
     * Deletar pool
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando pool de IP: ID={}", id);

        IpPool ipPool = ipPoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pool de IP não encontrado com ID: " + id));

        ipPoolRepository.delete(ipPool);
        log.info("Pool de IP deletado com sucesso: ID={}", id);
    }

    /**
     * Contar pools por servidor
     */
    @Transactional(readOnly = true)
    public long countByServer(Long serverId) {
        return ipPoolRepository.countByMikrotikServerId(serverId);
    }

    /**
     * Contar pools ativos por servidor
     */
    @Transactional(readOnly = true)
    public long countActiveByServer(Long serverId) {
        return ipPoolRepository.countByMikrotikServerIdAndActive(serverId, true);
    }
}
