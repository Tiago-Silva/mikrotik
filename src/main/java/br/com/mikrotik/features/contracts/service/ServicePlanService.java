package br.com.mikrotik.features.contracts.service;

import br.com.mikrotik.features.contracts.dto.ServicePlanDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.infrastructure.exception.ValidationException;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.contracts.model.ServicePlan;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeProfileRepository;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import br.com.mikrotik.features.contracts.repository.ServicePlanRepository;
import br.com.mikrotik.features.network.server.adapter.MikrotikSshService;
import br.com.mikrotik.shared.util.CompanyContextHolder;
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
public class ServicePlanService {

    private final ServicePlanRepository servicePlanRepository;
    private final PppoeProfileRepository pppoeProfileRepository;
    private final ContractRepository contractRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final MikrotikSshService mikrotikSshService;

    /**
     * Criar novo plano de serviço
     */
    @Transactional
    public ServicePlanDTO create(ServicePlanDTO dto) {
        log.info("Criando novo plano de serviço: {}", dto.getName());

        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }

        // Validar se o PPPoE Profile existe
        if (pppoeProfileRepository.findById(dto.getPppoeProfileId()).isEmpty()) {
            throw new ValidationException("PPPoE Profile não encontrado com ID: " + dto.getPppoeProfileId());
        }

        // Verificar se nome já existe na empresa
        if (servicePlanRepository.existsByNameAndCompanyId(dto.getName(), companyId)) {
            throw new ValidationException("Já existe um plano com este nome nesta empresa");
        }

        dto.setCompanyId(companyId);
        ServicePlan plan = dto.toEntity();
        plan = servicePlanRepository.save(plan);

        log.info("Plano de serviço criado com sucesso: ID={}", plan.getId());
        return ServicePlanDTO.fromEntity(plan);
    }

    /**
     * Buscar plano por ID
     */
    @Transactional(readOnly = true)
    public ServicePlanDTO findById(Long id) {
        log.info("Buscando plano de serviço por ID: {}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        ServicePlan plan = servicePlanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de serviço não encontrado com ID: " + id));

        return ServicePlanDTO.fromEntity(plan);
    }

    /**
     * Listar planos por empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ServicePlanDTO> findAll(Pageable pageable) {
        log.info("Listando planos de serviço");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ServicePlan> plans = servicePlanRepository.findByCompanyId(companyId, pageable);
        return plans.map(ServicePlanDTO::fromEntity);
    }

    /**
     * Listar planos ativos (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ServicePlanDTO> findByActive(Boolean active, Pageable pageable) {
        log.info("Listando planos de serviço ativos: {}", active);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ServicePlan> plans = servicePlanRepository.findByCompanyIdAndActive(companyId, active, pageable);
        return plans.map(ServicePlanDTO::fromEntity);
    }

    /**
     * Listar todos os planos da empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ServicePlanDTO> findAllByCompany(Pageable pageable) {
        log.info("Listando todos os planos da empresa");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ServicePlan> plans = servicePlanRepository.findByCompanyId(companyId, pageable);
        return plans.map(ServicePlanDTO::fromEntity);
    }

    /**
     * Listar planos ativos da empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ServicePlanDTO> findActiveByCompany(Pageable pageable) {
        log.info("Listando planos ativos da empresa");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ServicePlan> plans = servicePlanRepository.findByCompanyIdAndActive(companyId, true, pageable);
        return plans.map(ServicePlanDTO::fromEntity);
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<ServicePlanDTO> findByFilters(String name, Boolean active, Pageable pageable) {
        log.info("Buscando planos com filtros - nome: {}, ativo: {}", name, active);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<ServicePlan> plans = servicePlanRepository.findByFilters(companyId, name, active, pageable);
        return plans.map(ServicePlanDTO::fromEntity);
    }

    /**
     * Atualizar plano
     */
    @Transactional
    public ServicePlanDTO update(Long id, ServicePlanDTO dto) {
        log.info("Atualizando plano de serviço: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        ServicePlan existing = servicePlanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de serviço não encontrado com ID: " + id));

        // Verificar se nome foi alterado e já existe
        if (!existing.getName().equals(dto.getName())) {
            if (servicePlanRepository.existsByNameAndCompanyId(dto.getName(), companyId)) {
                throw new ValidationException("Já existe um plano com este nome nesta empresa");
            }
        }

        // Detectar mudança no perfil PPPoE
        boolean profileChanged = !existing.getPppoeProfileId().equals(dto.getPppoeProfileId());
        Long oldProfileId = existing.getPppoeProfileId();
        Long newProfileId = dto.getPppoeProfileId();

        // Validar perfil PPPoE se foi alterado
        if (profileChanged) {
            if (pppoeProfileRepository.findById(newProfileId).isEmpty()) {
                throw new ValidationException("Perfil PPPoE não encontrado");
            }
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setPppoeProfileId(dto.getPppoeProfileId());
        existing.setActive(dto.getActive());

        existing = servicePlanRepository.save(existing);

        log.info("Plano de serviço atualizado com sucesso: ID={}", id);

        // Sincronizar perfil com Mikrotik se foi alterado
        if (profileChanged) {
            log.info("==========================================================");
            log.info("PERFIL PPPoE ALTERADO - Sincronizando com Mikrotik");
            log.info("Plano: {} (ID: {})", existing.getName(), id);
            log.info("Perfil antigo ID: {}", oldProfileId);
            log.info("Perfil novo ID: {}", newProfileId);
            log.info("==========================================================");

            syncPppoeProfileToMikrotik(id, newProfileId);
        }

        return ServicePlanDTO.fromEntity(existing);
    }

    /**
     * Sincronizar perfil PPPoE com Mikrotik para todos os contratos ativos do plano
     */
    private void syncPppoeProfileToMikrotik(Long servicePlanId, Long newProfileId) {
        try {
            // Buscar novo perfil
            PppoeProfile newProfile = pppoeProfileRepository.findById(newProfileId)
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil PPPoE não encontrado"));

            // Buscar contratos ativos que usam este plano
            List<Contract> activeContracts = contractRepository.findByServicePlanIdAndStatus(
                    servicePlanId, Contract.ContractStatus.ACTIVE);

            if (activeContracts.isEmpty()) {
                log.info("Nenhum contrato ativo encontrado para este plano. Sincronização não necessária.");
                return;
            }

            log.info("Encontrados {} contrato(s) ativo(s) para sincronizar", activeContracts.size());

            int successCount = 0;
            int failureCount = 0;

            for (Contract contract : activeContracts) {
                if (contract.getPppoeUserId() == null) {
                    log.warn("Contrato ID {} não possui PPPoE User vinculado - Pulando", contract.getId());
                    continue;
                }

                try {
                    // Buscar usuário PPPoE
                    PppoeUser pppoeUser = pppoeUserRepository.findById(contract.getPppoeUserId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Usuário PPPoE não encontrado: " + contract.getPppoeUserId()));

                    log.info(">>> Sincronizando contrato ID {} - PPPoE User: {}",
                            contract.getId(), pppoeUser.getUsername());

                    // Alterar perfil no Mikrotik
                    mikrotikSshService.changePppoeUserProfile(
                            pppoeUser.getMikrotikServer().getIpAddress(),
                            pppoeUser.getMikrotikServer().getPort(),
                            pppoeUser.getMikrotikServer().getUsername(),
                            pppoeUser.getMikrotikServer().getPassword(),
                            pppoeUser.getUsername(),
                            newProfile.getName()
                    );

                    // Atualizar status no banco - não precisa mais salvar pppoeProfileId
                    pppoeUser.setUpdatedAt(LocalDateTime.now());
                    pppoeUserRepository.save(pppoeUser);

                    successCount++;
                    log.info("✅ Perfil sincronizado com sucesso - Contrato ID: {}", contract.getId());

                } catch (Exception e) {
                    failureCount++;
                    log.error("❌ Erro ao sincronizar contrato ID {}: {}", contract.getId(), e.getMessage());
                    // Continua processando os demais contratos
                }
            }

            log.info("==========================================================");
            log.info("SINCRONIZAÇÃO CONCLUÍDA");
            log.info("Total de contratos processados: {}", activeContracts.size());
            log.info("Sucessos: {}", successCount);
            log.info("Falhas: {}", failureCount);
            log.info("==========================================================");

        } catch (Exception e) {
            log.error("❌ Erro crítico ao sincronizar perfil com Mikrotik: {}", e.getMessage(), e);
            // Não lança exceção para não reverter a atualização do plano no banco
        }
    }

    /**
     * Ativar/Desativar plano
     */
    @Transactional
    public ServicePlanDTO toggleActive(Long id, Boolean active) {
        log.info("Alterando status do plano de serviço: ID={}, ativo={}", id, active);

        Long companyId = CompanyContextHolder.getCompanyId();
        ServicePlan plan = servicePlanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de serviço não encontrado com ID: " + id));

        plan.setActive(active);
        plan = servicePlanRepository.save(plan);

        log.info("Status do plano alterado com sucesso: ID={}", id);
        return ServicePlanDTO.fromEntity(plan);
    }

    /**
     * Deletar plano
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando plano de serviço: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        ServicePlan plan = servicePlanRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de serviço não encontrado com ID: " + id));

        servicePlanRepository.delete(plan);
        log.info("Plano de serviço deletado com sucesso: ID={}", id);
    }

    /**
     * Contar planos por empresa
     */
    @Transactional(readOnly = true)
    public long countByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return servicePlanRepository.countByCompanyId(companyId);
    }

    /**
     * Contar planos ativos por empresa
     */
    @Transactional(readOnly = true)
    public long countActiveByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return servicePlanRepository.countByCompanyIdAndActive(companyId, true);
    }
}
