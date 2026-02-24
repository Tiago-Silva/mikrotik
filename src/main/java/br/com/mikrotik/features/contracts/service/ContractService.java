package br.com.mikrotik.features.contracts.service;

import br.com.mikrotik.features.contracts.dto.ContractDTO;
import br.com.mikrotik.features.contracts.event.ContractStatusChangedEvent;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.shared.infrastructure.exception.ValidationException;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.contracts.model.ServicePlan;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.contracts.repository.ServicePlanRepository;
import br.com.mikrotik.features.customers.model.Address;
import br.com.mikrotik.features.customers.model.Customer;
import br.com.mikrotik.features.customers.repository.AddressRepository;
import br.com.mikrotik.features.customers.repository.CustomerRepository;
import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractService {

    private final ContractRepository contractRepository;
    private final CustomerRepository customerRepository;
    private final ServicePlanRepository servicePlanRepository;
    private final PppoeUserRepository pppoeUserRepository;
    private final AddressRepository addressRepository;
    private final MikrotikApiService mikrotikApiService; // API service for better performance
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

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

        // Validar usuário PPPoE se fornecido
        if (dto.getPppoeUserId() != null) {
            if (!pppoeUserRepository.findById(dto.getPppoeUserId()).isPresent()) {
                throw new ValidationException("Usuário PPPoE não encontrado");
            }
            // Verificar se usuário já está vinculado a outro contrato
            if (contractRepository.findByPppoeUserId(dto.getPppoeUserId()).isPresent()) {
                throw new ValidationException("Usuário PPPoE já está vinculado a outro contrato");
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
    public Page<ContractDTO> findByFilters(Long customerId,
                                           String customerName,
                                           Contract.ContractStatus status,
                                           Long servicePlanId,
                                           BigDecimal amountMin,
                                           BigDecimal amountMax,
                                           LocalDate createdFrom,
                                           LocalDate createdTo,
                                           Integer billingDayFrom,
                                           Integer billingDayTo,
                                           Pageable pageable) {
        log.info("Buscando contratos com filtros - customerName: {}, status: {}, servicePlanId: {}, " +
                 "amount: [{}-{}], createdAt: [{}-{}], billingDay: [{}-{}]",
                 customerName, status, servicePlanId, amountMin, amountMax,
                 createdFrom, createdTo, billingDayFrom, billingDayTo);

        Long companyId = CompanyContextHolder.getCompanyId();

        // Converte LocalDate → LocalDateTime para o range de createdAt
        LocalDateTime createdFromDt = createdFrom != null ? createdFrom.atStartOfDay() : null;
        // createdTo é exclusivo: início do dia seguinte
        LocalDateTime createdToDt = createdTo != null ? createdTo.plusDays(1).atStartOfDay() : null;

        Page<Contract> contracts = contractRepository.findByFilters(
                companyId, customerId, customerName, status, servicePlanId,
                amountMin, amountMax, createdFromDt, createdToDt,
                billingDayFrom, billingDayTo, pageable);

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

        // ── Plano de serviço: só atualiza se fornecido e diferente do atual ───────
        if (dto.getServicePlanId() != null && !dto.getServicePlanId().equals(existing.getServicePlanId())) {
            if (!servicePlanRepository.findByIdAndCompanyId(dto.getServicePlanId(), companyId).isPresent()) {
                throw new ValidationException("Plano de serviço não encontrado ou não pertence à empresa");
            }
            existing.setServicePlanId(dto.getServicePlanId());
        }

        // ── PPPoE User: PROTEÇÃO contra null-overwrite ────────────────────────────
        // Se o DTO não enviar pppoeUserId (null), o vínculo existente é preservado.
        // Só altera se vier um valor explícito E diferente do atual.
        if (dto.getPppoeUserId() != null && !dto.getPppoeUserId().equals(existing.getPppoeUserId())) {
            if (!pppoeUserRepository.findById(dto.getPppoeUserId()).isPresent()) {
                throw new ValidationException("Usuário PPPoE não encontrado");
            }
            if (contractRepository.findByPppoeUserId(dto.getPppoeUserId()).isPresent()) {
                throw new ValidationException("Usuário PPPoE já está vinculado a outro contrato");
            }
            existing.setPppoeUserId(dto.getPppoeUserId());
        }
        // ── Endereço de instalação: mesma proteção ────────────────────────────────
        if (dto.getInstallationAddressId() != null &&
            !dto.getInstallationAddressId().equals(existing.getInstallationAddressId())) {
            if (!addressRepository.existsById(dto.getInstallationAddressId())) {
                throw new ValidationException("Endereço de instalação não encontrado");
            }
            existing.setInstallationAddressId(dto.getInstallationAddressId());
        }

        // ── Campos obrigatórios (@NotNull no DTO) — sempre atualizados ────────────
        existing.setBillingDay(dto.getBillingDay());
        existing.setAmount(dto.getAmount());

        // ── endDate: nullable por design; só atualiza se o DTO trouxer valor ──────
        if (dto.getEndDate() != null) {
            existing.setEndDate(dto.getEndDate());
        }

        existing = contractRepository.save(existing);

        log.info("Contrato atualizado com sucesso: ID={}", id);
        return ContractDTO.fromEntity(existing);
    }

    /**
     * Alterar status do contrato
     *
     * ARQUITETURA: Publica evento para processamento assíncrono de integrações.
     * A transação é commitada ANTES de qualquer chamada ao Mikrotik.
     *
     * REGRA DE NEGÓCIO: O status do cliente é sincronizado junto ao contrato:
     *   ACTIVE              → Customer ACTIVE
     *   SUSPENDED_FINANCIAL → Customer SUSPENDED
     *   SUSPENDED_REQUEST   → Customer SUSPENDED
     *   CANCELED            → Customer CANCELED
     *   DRAFT / PENDING     → Cliente permanece inalterado
     */
    @Transactional
    public ContractDTO updateStatus(Long id, Contract.ContractStatus status) {
        log.info("Alterando status do contrato: ID={}, status={}", id, status);

        Long companyId = CompanyContextHolder.getCompanyId();
        Contract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        Contract.ContractStatus previousStatus = contract.getStatus();
        contract.setStatus(status);

        // Se cancelado, registrar data
        if (status == Contract.ContractStatus.CANCELED && contract.getCancellationDate() == null) {
            contract.setCancellationDate(LocalDate.now());
        }

        contract = contractRepository.save(contract);

        // ── Sincronizar status do cliente ────────────────────────────────────────
        syncCustomerStatus(contract.getCustomerId(), status);
        // ─────────────────────────────────────────────────────────────────────────

        // Publicar evento para processamento assíncrono (APÓS commit da transação)
        eventPublisher.publishEvent(new ContractStatusChangedEvent(
                this,
                contract.getId(),
                companyId,
                previousStatus,
                status,
                contract.getPppoeUserId(),
                "Status change via ContractService"
        ));

        log.info("Status do contrato alterado com sucesso: ID={}, evento publicado", id);
        return ContractDTO.fromEntity(contract);
    }

    /**
     * Mapeia status do contrato → status do cliente e persiste a atualização.
     * DRAFT / PENDING não afetam o cliente (ainda sem plano definitivo).
     */
    private void syncCustomerStatus(Long customerId, Contract.ContractStatus contractStatus) {
        Customer.CustomerStatus targetStatus = switch (contractStatus) {
            case ACTIVE                            -> Customer.CustomerStatus.ACTIVE;
            case SUSPENDED_FINANCIAL,
                 SUSPENDED_REQUEST                 -> Customer.CustomerStatus.SUSPENDED;
            case CANCELED                          -> Customer.CustomerStatus.CANCELED;
            default                                -> null; // DRAFT / PENDING: não altera
        };

        if (targetStatus == null) {
            log.debug("Status de contrato {} não requer alteração no cliente", contractStatus);
            return;
        }

        customerRepository.findById(customerId).ifPresentOrElse(customer -> {
            Customer.CustomerStatus previous = customer.getStatus();
            customer.setStatus(targetStatus);
            customerRepository.save(customer);
            log.info("Status do cliente ID={} sincronizado: {} → {}", customerId, previous, targetStatus);
        }, () -> log.warn("Cliente ID={} não encontrado ao sincronizar status", customerId));
    }

    /**
     * Ativar contrato
     *
     * FLUXO:
     * 1. Criar credencial PPPoE (se não existir) - SÍNCRONO pois é criação inicial
     * 2. Alterar status para ACTIVE - Publica evento
     * 3. NetworkIntegrationService processa desbloqueio assíncrono
     */
    @Transactional
    public ContractDTO activate(Long id) {
        log.info("=== INICIANDO ATIVAÇÃO DO CONTRATO {} ===", id);

        Long companyId = CompanyContextHolder.getCompanyId();
        log.info("Company ID: {}", companyId);

        Contract contract = contractRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado"));

        log.info("Contrato encontrado - ID: {}, Customer: {}, ServicePlan: {}, PppoeUserId: {}",
                contract.getId(), contract.getCustomerId(), contract.getServicePlanId(), contract.getPppoeUserId());

        // Se não tem usuário PPPoE vinculado, criar agora (SÍNCRONO - criação inicial)
        if (contract.getPppoeUserId() == null) {
            log.info(">>> PPPOE USER ID É NULL - CRIANDO CREDENCIAL PPPoE PARA CONTRATO {} <<<", id);
            try {
                createPppoeUserForContract(contract);
                log.info(">>> CREDENCIAL PPPoE CRIADA COM SUCESSO <<<");
            } catch (Exception e) {
                log.error("!!! ERRO AO CRIAR CREDENCIAL PPPoE: {} !!!", e.getMessage(), e);
                throw e;
            }
        } else {
            log.info("Contrato JÁ possui PPPoE User vinculado: {}", contract.getPppoeUserId());
        }

        // Atualizar status (publica evento para desbloqueio assíncrono)
        log.info("Atualizando status do contrato para ACTIVE");
        ContractDTO contractDTO = updateStatus(id, Contract.ContractStatus.ACTIVE);

        log.info("=== ATIVAÇÃO DO CONTRATO {} CONCLUÍDA (integração será processada assíncronamente) ===", id);
        return contractDTO;
    }

    /**
     * Suspender contrato por inadimplência
     *
     * PROTEÇÃO TRANSACIONAL:
     * - Altera status no banco (@Transactional)
     * - Fecha transação
     * - NetworkIntegrationService processa bloqueio assíncrono (@Async)
     */
    @Transactional
    public ContractDTO suspendFinancial(Long id) {
        log.info("=== SUSPENDENDO CONTRATO POR INADIMPLÊNCIA - ID: {} ===", id);

        ContractDTO contract = updateStatus(id, Contract.ContractStatus.SUSPENDED_FINANCIAL);
        log.info("Status alterado para: SUSPENDED_FINANCIAL (bloqueio será processado assíncrono)");

        log.info("=== SUSPENSÃO CONCLUÍDA ===");
        return contract;
    }

    /**
     * Suspender contrato por solicitação
     */
    @Transactional
    public ContractDTO suspendByRequest(Long id) {
        log.info("=== SUSPENDENDO CONTRATO POR SOLICITAÇÃO - ID: {} ===", id);

        ContractDTO contract = updateStatus(id, Contract.ContractStatus.SUSPENDED_REQUEST);
        log.info("Status alterado para: SUSPENDED_REQUEST (bloqueio será processado assíncronamente)");

        return contract;
    }

    /**
     * Cancelar contrato
     */
    @Transactional
    public ContractDTO cancel(Long id) {
        log.info("=== CANCELANDO CONTRATO - ID: {} ===", id);

        ContractDTO contract = updateStatus(id, Contract.ContractStatus.CANCELED);
        log.info("Status alterado para: CANCELED (deleção será processada assíncronamente)");

        log.info("=== CANCELAMENTO CONCLUÍDO ===");
        return contract;
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

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Criar usuário PPPoE para o contrato
     *
     * NOTA: Este método é SÍNCRONO pois é executado apenas na ativação inicial.
     * Criação de credencial é diferente de bloqueio/desbloqueio (que são assíncronos).
     */
    private void createPppoeUserForContract(Contract contract) {
        log.info(">>> ENTRANDO EM createPppoeUserForContract - Contrato ID: {} <<<", contract.getId());
        try {
            // Buscar plano de serviço
            log.info("Buscando plano de serviço ID: {}", contract.getServicePlanId());
            ServicePlan servicePlan = servicePlanRepository.findById(contract.getServicePlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plano de serviço não encontrado"));
            log.info("Plano encontrado: {}", servicePlan.getName());

            PppoeProfile profile = servicePlan.getPppoeProfile();
            if (profile == null) {
                log.error("!!! ERRO: Plano de serviço não possui perfil PPPoE configurado !!!");
                throw new ValidationException("Plano de serviço não possui perfil PPPoE configurado");
            }
            log.info("Perfil PPPoE encontrado: {} (ID: {})", profile.getName(), profile.getId());

            MikrotikServer server = profile.getMikrotikServer();
            log.info("Servidor Mikrotik: {} ({}:{})", server.getName(), server.getIpAddress(), server.getPort());

            // Buscar cliente
            log.info("Buscando cliente ID: {}", contract.getCustomerId());
            Customer customer = customerRepository.findById(contract.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
            log.info("Cliente encontrado: {}", customer.getName());

            // Buscar endereço de instalação
            String enderecoCompleto = "";
            if (contract.getInstallationAddressId() != null) {
                log.info("Buscando endereço de instalação ID: {}", contract.getInstallationAddressId());
                Address installationAddress = addressRepository.findById(contract.getInstallationAddressId())
                        .orElse(null);
                if (installationAddress != null) {
                    enderecoCompleto = String.format("%s, %s - %s, %s/%s",
                            installationAddress.getStreet(),
                            installationAddress.getNumber(),
                            installationAddress.getDistrict(),
                            installationAddress.getCity(),
                            installationAddress.getState());
                    log.info("Endereço encontrado: {}", enderecoCompleto);
                } else {
                    log.warn("Endereço de instalação não encontrado no banco");
                }
            } else {
                log.warn("Contrato não possui installationAddressId");
            }

            // Gerar username único
            log.info("Gerando username único...");
            String username = generateUniqueUsername(customer);
            String password = generateSecurePassword();
            log.info("Username gerado: {}", username);

            // Montar comentário completo
            String comentario = String.format("Contrato #%d - %s - %s",
                    contract.getId(),
                    customer.getName(),
                    enderecoCompleto.isEmpty() ? "Endereço não informado" : enderecoCompleto);
            log.info("Comentário: {}", comentario);

            // 1. Criar no Mikrotik via API COM endereço no comentário
            log.info(">>> CRIANDO USUÁRIO NO MIKROTIK VIA API <<<");
            mikrotikApiService.createPppoeUserWithComment(
                    server.getIpAddress(),
                    server.getApiPort(),
                    server.getUsername(),
                    server.getPassword(),
                    username,
                    password,
                    profile.getName(),
                    comentario
            );
            log.info(">>> USUÁRIO CRIADO NO MIKROTIK COM SUCESSO <<<");

            // 2. Salvar no banco
            log.info(">>> SALVANDO USUÁRIO NO BANCO DE DADOS <<<");
            PppoeUser pppoeUser = new PppoeUser();
            pppoeUser.setCompanyId(contract.getCompanyId());
            pppoeUser.setUsername(username);
            pppoeUser.setPassword(passwordEncoder.encode(password));
            pppoeUser.setEmail(customer.getEmail() != null ? customer.getEmail() : username + "@cliente.local");
            pppoeUser.setComment(comentario);
            pppoeUser.setStatus(PppoeUser.UserStatus.OFFLINE);
            pppoeUser.setActive(true);
            pppoeUser.setProfile(profile);
            pppoeUser.setMikrotikServer(server);
            pppoeUser.setCreatedAt(LocalDateTime.now());
            pppoeUser.setUpdatedAt(LocalDateTime.now());

            PppoeUser saved = pppoeUserRepository.save(pppoeUser);
            log.info(">>> USUÁRIO SALVO NO BANCO - ID: {} <<<", saved.getId());

            // 3. Vincular ao contrato
            log.info(">>> VINCULANDO USUÁRIO PPPoE AO CONTRATO <<<");
            contract.setPppoeUserId(saved.getId());
            contractRepository.save(contract);
            log.info(">>> CONTRATO ATUALIZADO - pppoeUserId: {} <<<", saved.getId());

            log.info("==========================================================");
            log.info("✅ CREDENCIAL PPPoE CRIADA COM SUCESSO!");
            log.info("Username: {}", username);
            log.info("Senha: {}", password);
            log.info("Perfil: {}", profile.getName());
            log.info("Comentário: {}", comentario);
            log.info("==========================================================");
            log.info("⚠️  IMPORTANTE: Enviar estas credenciais ao cliente!");
            log.info("==========================================================");

            // TODO: Implementar envio de email/SMS com as credenciais para o cliente

        } catch (Exception e) {
            log.error("==========================================================");
            log.error("❌ ERRO AO CRIAR CREDENCIAL PPPoE PARA CONTRATO {}", contract.getId());
            log.error("Tipo de erro: {}", e.getClass().getSimpleName());
            log.error("Mensagem: {}", e.getMessage());
            log.error("Stack trace:", e);
            log.error("==========================================================");
            throw new RuntimeException("Erro ao criar credencial PPPoE: " + e.getMessage(), e);
        }
    }

    /**
     * Gerar username único baseado no cliente
     */
    private String generateUniqueUsername(Customer customer) {
        // Pegar nome do cliente e limpar caracteres especiais
        String baseName = customer.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .replaceAll("\\s+", "");

        // Limitar tamanho
        if (baseName.length() > 32) {
            baseName = baseName.substring(0, 32);
        }

        // Se ficou vazio, usar "cliente"
        if (baseName.isEmpty()) {
            baseName = "cliente";
        }

        String username = baseName;
        int counter = 1;

        // Garantir unicidade
        while (pppoeUserRepository.findByUsername(username).isPresent()) {
            username = baseName + counter++;
        }

        return username;
    }

    /**
     * Gerar senha segura aleatória
     */
    private String generateSecurePassword() {
        // Caracteres sem ambiguidade (sem I, l, 1, O, 0)
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
