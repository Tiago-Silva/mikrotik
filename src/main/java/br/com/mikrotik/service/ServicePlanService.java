package br.com.mikrotik.service;

import br.com.mikrotik.dto.ServicePlanDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.ServicePlan;
import br.com.mikrotik.repository.InternetProfileRepository;
import br.com.mikrotik.repository.ServicePlanRepository;
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
public class ServicePlanService {

    private final ServicePlanRepository servicePlanRepository;
    private final InternetProfileRepository internetProfileRepository;

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

        // Validar se internet profile existe e pertence à empresa
        if (!internetProfileRepository.findByIdAndCompanyId(dto.getInternetProfileId(), companyId).isPresent()) {
            throw new ValidationException("Perfil de internet não encontrado ou não pertence à empresa");
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

        // Validar internet profile se foi alterado
        if (!existing.getInternetProfileId().equals(dto.getInternetProfileId())) {
            if (!internetProfileRepository.findByIdAndCompanyId(dto.getInternetProfileId(), companyId).isPresent()) {
                throw new ValidationException("Perfil de internet não encontrado ou não pertence à empresa");
            }
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setInternetProfileId(dto.getInternetProfileId());
        existing.setActive(dto.getActive());

        existing = servicePlanRepository.save(existing);

        log.info("Plano de serviço atualizado com sucesso: ID={}", id);
        return ServicePlanDTO.fromEntity(existing);
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
