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
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final MikrotikApiService mikrotikApiService;

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

        final ServicePlan saved = servicePlanRepository.save(existing);

        log.info("Plano de serviço atualizado com sucesso: ID={}", id);

        // REGRA DE OURO: registrar a sincronização para APÓS o commit da transação.
        // Sem isso, cada chamada ao Mikrotik segura a connection pool durante toda a iteração.
        // Com N contratos ativos, isso é N round-trips de rede dentro de 1 transação aberta.
        if (profileChanged) {
            log.info("Perfil PPPoE alterado no plano {} — sincronização com Mikrotik agendada para após commit", id);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    syncPppoeProfileToMikrotik(saved.getId(), newProfileId);
                }
            });
        }

        return ServicePlanDTO.fromEntity(saved);
    }

    /**
     * Sincronizar perfil PPPoE com Mikrotik para todos os contratos ativos do plano.
     * Executado APÓS o commit da transação principal via afterCommit().
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
                log.info("Nenhum contrato ativo encontrado para o plano {}. Sincronização não necessária.", servicePlanId);
                return;
            }

            log.info("Sincronizando perfil PPPoE para {} contrato(s) ativo(s) do plano {}", activeContracts.size(), servicePlanId);

            int successCount = 0;
            int failureCount = 0;

            for (Contract contract : activeContracts) {
                if (contract.getPppoeUserId() == null) {
                    log.warn("Contrato ID {} não possui PPPoE User vinculado - Pulando", contract.getId());
                    continue;
                }

                try {
                    PppoeUser pppoeUser = pppoeUserRepository.findById(contract.getPppoeUserId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Usuário PPPoE não encontrado: " + contract.getPppoeUserId()));

                    log.info(">>> Sincronizando contrato ID {} - PPPoE User: {}", contract.getId(), pppoeUser.getUsername());

                    // Alterar perfil no Mikrotik via API
                    mikrotikApiService.changePppoeUserProfile(
                            pppoeUser.getMikrotikServer().getIpAddress(),
                            pppoeUser.getMikrotikServer().getApiPort(),
                            pppoeUser.getMikrotikServer().getUsername(),
                            pppoeUser.getMikrotikServer().getPassword(),
                            pppoeUser.getUsername(),
                            newProfile.getName()
                    );

                    // Atualizar perfil do pppoe_user no banco para manter consistência
                    pppoeUser.setProfile(newProfile);
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
            log.info("SINCRONIZAÇÃO CONCLUÍDA — Plano ID: {}", servicePlanId);
            log.info("Contratos processados: {} | Sucessos: {} | Falhas: {}",
                    activeContracts.size(), successCount, failureCount);
            log.info("==========================================================");

        } catch (Exception e) {
            log.error("❌ Erro crítico ao sincronizar perfil com Mikrotik: {}", e.getMessage(), e);
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
