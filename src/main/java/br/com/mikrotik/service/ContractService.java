package br.com.mikrotik.service;

import br.com.mikrotik.dto.ContractDTO;
import br.com.mikrotik.exception.ResourceNotFoundException;
import br.com.mikrotik.exception.ValidationException;
import br.com.mikrotik.model.Contract;
import br.com.mikrotik.repository.*;
import br.com.mikrotik.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final CustomerRepository customerRepository;
    private final ServicePlanRepository servicePlanRepository;
    private final PppoeCredentialRepository pppoeCredentialRepository;
    private final AddressRepository addressRepository;

    /**
     * Criar novo contrato
     */
    @Transactional
    public ContractDTO create(ContractDTO dto) {
        log.info("Criando novo contrato para cliente: {}", dto.getCustomerId());

        Long companyId = CompanyContextHolder.getCompanyId();
        if (companyId == null) {
            throw new ValidationException("Company ID não encontrado no contexto");
        }

        // Validar se cliente existe e pertence à empresa
        if (!customerRepository.findByIdAndCompanyId(dto.getCustomerId(), companyId).isPresent()) {
            throw new ValidationException("Cliente não encontrado ou não pertence à empresa");
        }

        // Validar se plano de serviço existe e pertence à empresa
        if (!servicePlanRepository.findByIdAndCompanyId(dto.getServicePlanId(), companyId).isPresent()) {
            throw new ValidationException("Plano de serviço não encontrado ou não pertence à empresa");
        }

        // Validar credencial PPPoE se fornecida
        if (dto.getPppoeCredentialId() != null) {
            if (!pppoeCredentialRepository.findByIdAndCompanyId(dto.getPppoeCredentialId(), companyId).isPresent()) {
                throw new ValidationException("Credencial PPPoE não encontrada ou não pertence à empresa");
            }
            // Verificar se credencial já está vinculada a outro contrato
            if (contractRepository.findByPppoeCredentialId(dto.getPppoeCredentialId()).isPresent()) {
                throw new ValidationException("Credencial PPPoE já está vinculada a outro contrato");
            }
        }

        // Validar endereço de instalação se fornecido
        if (dto.getInstallationAddressId() != null) {
            if (!addressRepository.existsById(dto.getInstallationAddressId())) {
                throw new ValidationException("Endereço de instalação não encontrado");
            }
        }

        dto.setCompanyId(companyId);
        Contract contract = dto.toEntity();
        contract = contractRepository.save(contract);

        log.info("Contrato criado com sucesso: ID={}", contract.getId());
        return ContractDTO.fromEntity(contract);
    }

    /**
     * Buscar contrato por ID
     */
    @Transactional(readOnly = true)
    public ContractDTO findById(Long id) {
        log.info("Buscando contrato por ID: {}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Contract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        return ContractDTO.fromEntity(contract);
    }

    /**
     * Listar contratos por empresa (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ContractDTO> findAll(Pageable pageable) {
        log.info("Listando contratos");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Contract> contracts = contractRepository.findByCompanyId(companyId, pageable);
        return contracts.map(ContractDTO::fromEntity);
    }

    /**
     * Listar contratos por cliente (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ContractDTO> findByCustomer(Long customerId, Pageable pageable) {
        log.info("Listando contratos do cliente: {}", customerId);

        Page<Contract> contracts = contractRepository.findByCustomerId(customerId, pageable);
        return contracts.map(ContractDTO::fromEntity);
    }

    /**
     * Listar contratos por status (paginado)
     */
    @Transactional(readOnly = true)
    public Page<ContractDTO> findByStatus(Contract.ContractStatus status, Pageable pageable) {
        log.info("Listando contratos por status: {}", status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Contract> contracts = contractRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return contracts.map(ContractDTO::fromEntity);
    }

    /**
     * Buscar com filtros múltiplos
     */
    @Transactional(readOnly = true)
    public Page<ContractDTO> findByFilters(Long customerId, Contract.ContractStatus status,
                                           Long servicePlanId, Pageable pageable) {
        log.info("Buscando contratos com filtros");

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Contract> contracts = contractRepository.findByFilters(companyId, customerId, status, servicePlanId, pageable);
        return contracts.map(ContractDTO::fromEntity);
    }

    /**
     * Atualizar contrato
     */
    @Transactional
    public ContractDTO update(Long id, ContractDTO dto) {
        log.info("Atualizando contrato: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Contract existing = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        // Validar credencial PPPoE se foi alterada
        if (dto.getPppoeCredentialId() != null &&
            !dto.getPppoeCredentialId().equals(existing.getPppoeCredentialId())) {
            if (!pppoeCredentialRepository.findByIdAndCompanyId(dto.getPppoeCredentialId(), companyId).isPresent()) {
                throw new ValidationException("Credencial PPPoE não encontrada ou não pertence à empresa");
            }
            if (contractRepository.findByPppoeCredentialId(dto.getPppoeCredentialId()).isPresent()) {
                throw new ValidationException("Credencial PPPoE já está vinculada a outro contrato");
            }
        }

        existing.setPppoeCredentialId(dto.getPppoeCredentialId());
        existing.setInstallationAddressId(dto.getInstallationAddressId());
        existing.setBillingDay(dto.getBillingDay());
        existing.setAmount(dto.getAmount());
        existing.setEndDate(dto.getEndDate());

        existing = contractRepository.save(existing);

        log.info("Contrato atualizado com sucesso: ID={}", id);
        return ContractDTO.fromEntity(existing);
    }

    /**
     * Alterar status do contrato
     */
    @Transactional
    public ContractDTO updateStatus(Long id, Contract.ContractStatus status) {
        log.info("Alterando status do contrato: ID={}, status={}", id, status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Contract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        contract.setStatus(status);

        // Se cancelado, registrar data
        if (status == Contract.ContractStatus.CANCELED && contract.getCancellationDate() == null) {
            contract.setCancellationDate(LocalDate.now());
        }

        contract = contractRepository.save(contract);

        log.info("Status do contrato alterado com sucesso: ID={}", id);
        return ContractDTO.fromEntity(contract);
    }

    /**
     * Ativar contrato
     */
    @Transactional
    public ContractDTO activate(Long id) {
        return updateStatus(id, Contract.ContractStatus.ACTIVE);
    }

    /**
     * Suspender contrato por inadimplência
     */
    @Transactional
    public ContractDTO suspendFinancial(Long id) {
        return updateStatus(id, Contract.ContractStatus.SUSPENDED_FINANCIAL);
    }

    /**
     * Suspender contrato por solicitação
     */
    @Transactional
    public ContractDTO suspendByRequest(Long id) {
        return updateStatus(id, Contract.ContractStatus.SUSPENDED_REQUEST);
    }

    /**
     * Cancelar contrato
     */
    @Transactional
    public ContractDTO cancel(Long id) {
        return updateStatus(id, Contract.ContractStatus.CANCELED);
    }

    /**
     * Deletar contrato
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deletando contrato: ID={}", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        Contract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        contractRepository.delete(contract);
        log.info("Contrato deletado com sucesso: ID={}", id);
    }

    /**
     * Buscar contratos para faturamento (por dia de cobrança) - paginado
     */
    @Transactional(readOnly = true)
    public Page<ContractDTO> findContractsForBilling(Integer billingDay, Pageable pageable) {
        log.info("Buscando contratos para faturamento - dia: {}", billingDay);

        Long companyId = CompanyContextHolder.getCompanyId();
        Page<Contract> contracts = contractRepository.findContractsForBilling(companyId, billingDay, pageable);
        return contracts.map(ContractDTO::fromEntity);
    }

    /**
     * Contar contratos por empresa
     */
    @Transactional(readOnly = true)
    public long countByCompany() {
        Long companyId = CompanyContextHolder.getCompanyId();
        return contractRepository.countByCompanyId(companyId);
    }

    /**
     * Contar contratos por status
     */
    @Transactional(readOnly = true)
    public long countByStatus(Contract.ContractStatus status) {
        Long companyId = CompanyContextHolder.getCompanyId();
        return contractRepository.countByCompanyIdAndStatus(companyId, status);
    }
}
