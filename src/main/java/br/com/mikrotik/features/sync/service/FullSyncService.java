package br.com.mikrotik.features.sync.service;

import br.com.mikrotik.features.sync.dto.FullSyncConfigDTO;
import br.com.mikrotik.features.sync.dto.FullSyncResultDTO;
import br.com.mikrotik.features.sync.dto.SyncResultDTO;
import br.com.mikrotik.features.sync.dto.CustomerInfoParseResult;
import br.com.mikrotik.features.sync.dto.ParsePreviewDTO;
import br.com.mikrotik.features.sync.dto.ParsePreviewDTO.ParsePreviewItemDTO;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.customers.model.Customer;
import br.com.mikrotik.features.customers.model.Address;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.contracts.model.ServicePlan;
import br.com.mikrotik.features.network.server.repository.MikrotikServerRepository;
import br.com.mikrotik.features.network.pppoe.service.PppoeProfileService;
import br.com.mikrotik.features.network.pppoe.service.PppoeUserService;
import br.com.mikrotik.features.contracts.service.ContractService;
import br.com.mikrotik.features.network.pppoe.repository.PppoeProfileRepository;
import br.com.mikrotik.features.customers.repository.CustomerRepository;
import br.com.mikrotik.features.customers.repository.AddressRepository;
import br.com.mikrotik.features.contracts.repository.ContractRepository;
import br.com.mikrotik.features.contracts.repository.ServicePlanRepository;
import br.com.mikrotik.features.contracts.dto.ContractDTO;
import br.com.mikrotik.shared.util.CompanyContextHolder;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class FullSyncService {


    private final MikrotikServerRepository serverRepository;
    private final PppoeProfileService profileService;
    private final PppoeUserService pppoeUserService;
    private final ServicePlanRepository servicePlanRepository;
    private final PppoeProfileRepository profileRepository;
    private final CustomerRepository customerRepository;
    private final ContractRepository contractRepository;
    private final ContractService contractService;
    private final AddressRepository addressRepository;
    private final EntityManager entityManager;

    /**
     * Pré-visualização do parsing: retorna o que seria criado para cada PPPoE
     * SEM persistir nada. Útil para validar antes de executar o fullSync.
     */
    @Transactional(readOnly = true)
    public ParsePreviewDTO parsePreview(Long serverId) {
        Long companyId = CompanyContextHolder.getCompanyId();
        List<PppoeUser> users = pppoeUserService.findAll();
        List<ParsePreviewItemDTO> items = new ArrayList<>();

        for (PppoeUser user : users) {
            CustomerInfoParseResult parsed = parseCustomerInfo(user);

            boolean alreadySynced = contractRepository.findByPppoeUserIdAndCompanyId(
                    user.getId(), companyId).isPresent();

            items.add(ParsePreviewItemDTO.builder()
                    .pppoeUsername(user.getUsername())
                    .resolvedCustomerName(parsed.getCustomerName())
                    .originalComment(parsed.getOriginalComment())
                    .parsedStreet(parsed.getAddress())
                    .parsedNumber(parsed.getAddressNumber())
                    .parsedNeighborhood(parsed.getNeighborhood())
                    .parsedPhone(parsed.getPhone())
                    .profile(user.getProfile() != null ? user.getProfile().getName() : null)
                    .alreadySynced(alreadySynced)
                    .warning(parsed.getWarningMessage())
                    .build());
        }

        log.info("Parse preview: {} usuários analisados (servidor: {})", items.size(), serverId);
        return ParsePreviewDTO.builder()
                .total(items.size())
                .items(items)
                .build();
    }

    /**
     * Sincronização completa: Profiles → ServicePlans → PPPoE Users → Customers → Contracts
     * ⚠️ OPERAÇÃO APENAS DE LEITURA NO MIKROTIK - NÃO MODIFICA NADA NO ROTEADOR
     */
    @Transactional
    public FullSyncResultDTO fullSync(FullSyncConfigDTO config) {
        long startTime = System.currentTimeMillis();

        log.info("==========================================================");
        log.info(">>> INICIANDO SINCRONIZAÇÃO COMPLETA <<<");
        log.info("Servidor ID: {}", config.getServerId());
        log.info("==========================================================");

        FullSyncResultDTO result = FullSyncResultDTO.builder().build();
        Long companyId = CompanyContextHolder.getCompanyId();

        // ✅ Mapa em memória: pppoeUserId → customerId
        // Substitui o hack [CUSTOMER_ID:xxx] no campo comment.
        // O entityManager.clear() entre fases descartava o objeto em memória antes
        // que a Fase 5 pudesse lê-lo — o Map persiste durante toda a execução do sync.
        Map<Long, Long> pppoeUserToCustomerMap = new HashMap<>();

        try {
            // Validar servidor
            MikrotikServer server = serverRepository.findById(config.getServerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Servidor MikroTik não encontrado"));

            log.info("Servidor encontrado: {} ({}:{})", server.getName(), server.getIpAddress(), server.getApiPort());

            // FASE 1: Sincronizar Profiles
            log.info("\n--- FASE 1: Sincronizando Profiles ---");
            syncProfilesPhase(config, result);

            // ⚠️ CRÍTICO: Flush e clear após cada fase
            entityManager.flush();
            entityManager.clear();

            // FASE 2: Criar Service Plans para Profiles sem plano
            if (config.getCreateMissingServicePlans()) {
                log.info("\n--- FASE 2: Criando Planos de Serviço ---");
                createServicePlansPhase(config, result, companyId);

                // ⚠️ CRÍTICO: Flush e clear após criar planos
                entityManager.flush();
                entityManager.clear();
            }

            // FASE 3: Sincronizar Usuários PPPoE
            log.info("\n--- FASE 3: Sincronizando Usuários PPPoE ---");
            syncPppoeUsersPhase(config, result);

            // ⚠️ CRÍTICO: Flush e clear após sincronizar usuários
            entityManager.flush();
            entityManager.clear();

            // FASE 4: Criar Clientes a partir dos comentários PPPoE
            if (config.getCreateMissingCustomers()) {
                log.info("\n--- FASE 4: Criando Clientes ---");
                createCustomersPhase(result, companyId, pppoeUserToCustomerMap);

                // ⚠️ CRÍTICO: Flush e clear após criar clientes
                entityManager.flush();
                entityManager.clear();
            }

            // FASE 5: Criar Contratos
            if (config.getCreateContracts()) {
                log.info("\n--- FASE 5: Criando Contratos ---");
                createContractsPhase(config, result, companyId, pppoeUserToCustomerMap);

                // ⚠️ CRÍTICO: Flush final
                entityManager.flush();
            }

            result.setSuccess(true);

        } catch (Exception e) {
            log.error("Erro durante sincronização completa: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.getErrorMessages().add("Erro crítico: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        result.setExecutionTimeSeconds((endTime - startTime) / 1000);

        log.info("\n==========================================================");
        log.info(">>> SINCRONIZAÇÃO COMPLETA FINALIZADA <<<");
        log.info("Tempo: {}s | Sucesso: {}", result.getExecutionTimeSeconds(), result.getSuccess());
        log.info("Profiles: {}/{}", result.getSyncedProfiles(), result.getTotalProfiles());
        log.info("Planos: {} criados", result.getCreatedServicePlans());
        log.info("PPPoE Users: {}/{}", result.getSyncedPppoeUsers(), result.getTotalPppoeUsers());
        log.info("Clientes: {} criados", result.getCreatedCustomers());
        log.info("Contratos: {} criados, {} ativados, {} suspensos",
                result.getCreatedContracts(), result.getActivatedContracts(), result.getSuspendedContracts());
        log.info("==========================================================");

        return result;
    }

    /**
     * FASE 1: Sincronizar Profiles do MikroTik
     * ⚠️ APENAS LEITURA - Não modifica nada no MikroTik
     */
    private void syncProfilesPhase(FullSyncConfigDTO config, FullSyncResultDTO result) {
        try {
            SyncResultDTO profileSyncResult = profileService.syncProfilesFromMikrotik(config.getServerId());

            result.setTotalProfiles(profileSyncResult.getTotalMikrotikUsers()); // Reusa o campo
            result.setSyncedProfiles(profileSyncResult.getSyncedUsers());
            result.setSkippedProfiles(profileSyncResult.getSkippedUsers());
            result.getSyncedProfileNames().addAll(profileSyncResult.getSyncedUsernames());

            if (!profileSyncResult.getErrorMessages().isEmpty()) {
                result.getErrorMessages().addAll(profileSyncResult.getErrorMessages());
            }

            log.info("Profiles sincronizados: {}/{}", result.getSyncedProfiles(), result.getTotalProfiles());

        } catch (Exception e) {
            log.error("Erro ao sincronizar profiles: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro ao sincronizar profiles: " + e.getMessage());
        }
    }

    /**
     * FASE 2: Criar Service Plans para Profiles que não têm plano
     */
    private void createServicePlansPhase(FullSyncConfigDTO config, FullSyncResultDTO result, Long companyId) {
        try {
            List<PppoeProfile> allProfiles = profileRepository.findAll();

            for (PppoeProfile profile : allProfiles) {
                try {
                    // Verificar se já existe um ServicePlan para este profile
                    Optional<ServicePlan> existingPlan = servicePlanRepository.findByPppoeProfileIdAndCompanyId(
                            profile.getId(), companyId);

                    if (existingPlan.isPresent()) {
                        result.setExistingServicePlans(result.getExistingServicePlans() + 1);
                        log.debug("Plano já existe para profile: {}", profile.getName());
                    } else {
                        // ⚠️ CRÍTICO: Recarregar o profile do banco para garantir que está attached
                        PppoeProfile attachedProfile = profileRepository.findById(profile.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Profile não encontrado: " + profile.getId()));

                        // Criar ServicePlan automaticamente
                        ServicePlan newPlan = ServicePlan.builder()
                                .companyId(companyId)
                                .name("Plano " + attachedProfile.getName())
                                .description("Plano criado automaticamente na sincronização")
                                .price(config.getDefaultPlanPrice())
                                .pppoeProfileId(attachedProfile.getId()) // ⚠️ CRÍTICO: Definir ID manualmente
                                .active(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        servicePlanRepository.save(newPlan);
                        entityManager.flush(); // ⚠️ CRÍTICO: Flush para garantir que ID seja gerado
                        entityManager.clear(); // ⚠️ CRÍTICO: Limpar sessão para evitar conflitos

                        result.setCreatedServicePlans(result.getCreatedServicePlans() + 1);
                        result.getCreatedServicePlanNames().add(newPlan.getName());

                        log.info("Plano criado: {} (ID: {}, R$ {}) para profile: {}",
                                newPlan.getName(), newPlan.getId(), newPlan.getPrice(), attachedProfile.getName());
                    }
                } catch (Exception e) {
                    log.error("Erro ao criar plano para profile {}: {}", profile.getName(), e.getMessage());
                    result.getErrorMessages().add("Erro ao criar plano para " + profile.getName() + ": " + e.getMessage());
                }
            }

            log.info("Planos de serviço: {} criados, {} já existiam",
                    result.getCreatedServicePlans(), result.getExistingServicePlans());

        } catch (Exception e) {
            log.error("Erro ao criar planos de serviço: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro ao criar planos: " + e.getMessage());
        }
    }

    /**
     * FASE 3: Sincronizar Usuários PPPoE
     * ⚠️ APENAS LEITURA - Não modifica nada no MikroTik
     */
    private void syncPppoeUsersPhase(FullSyncConfigDTO config, FullSyncResultDTO result) {
        try {
            SyncResultDTO userSyncResult = pppoeUserService.syncUsersFromMikrotik(config.getServerId(), null);

            result.setTotalPppoeUsers(userSyncResult.getTotalMikrotikUsers());
            result.setSyncedPppoeUsers(userSyncResult.getSyncedUsers());
            result.setSkippedPppoeUsers(userSyncResult.getSkippedUsers());
            result.getSyncedPppoeUsernames().addAll(userSyncResult.getSyncedUsernames());

            if (!userSyncResult.getErrorMessages().isEmpty()) {
                result.getErrorMessages().addAll(userSyncResult.getErrorMessages());
            }

            log.info("Usuários PPPoE sincronizados: {}/{}", result.getSyncedPppoeUsers(), result.getTotalPppoeUsers());

        } catch (Exception e) {
            log.error("Erro ao sincronizar usuários PPPoE: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro ao sincronizar usuários: " + e.getMessage());
        }
    }

    /**
     * FASE 4: Criar Clientes a partir dos usuários PPPoE
     * <p>
     * REGRA DE PARSING (baseado nos dados reais do MikroTik):
     * - USERNAME  → nome do cliente  (ex: "elianabatista" → "Eliana Batista")
     * - COMMENT   → endereço/localização (ex: "rua 1 n120", "manoel b paixa n63")
     * <p>
     * O mapa pppoeUserToCustomerMap é preenchido aqui e consumido na Fase 5,
     * eliminando o hack de escrita no campo comment (que era perdido após entityManager.clear()).
     */
    private void createCustomersPhase(FullSyncResultDTO result, Long companyId,
                                      Map<Long, Long> pppoeUserToCustomerMap) {
        try {
            List<PppoeUser> allPppoeUsers = pppoeUserService.findAll();

            for (PppoeUser pppoeUser : allPppoeUsers) {
                try {
                    // ── 1. Re-sync: contrato já existe → registrar no mapa e pular ──
                    Optional<Contract> existingContract = contractRepository.findByPppoeUserIdAndCompanyId(
                            pppoeUser.getId(), companyId);

                    if (existingContract.isPresent()) {
                        Long existingCustomerId = existingContract.get().getCustomerId();
                        pppoeUserToCustomerMap.put(pppoeUser.getId(), existingCustomerId);
                        result.setExistingCustomers(result.getExistingCustomers() + 1);
                        log.debug("Contrato já existe para PPPoE: {} → customerId: {}",
                                pppoeUser.getUsername(), existingCustomerId);
                        continue;
                    }

                    // ── 2. Parse: username = nome; comment = endereço ──
                    CustomerInfoParseResult parseResult = parseCustomerInfo(pppoeUser);

                    if (parseResult.getWarningMessage() != null) {
                        result.getWarnings().add(parseResult.getWarningMessage());
                    }

                    // ── 3. Deduplicação por nome normalizado ──
                    Optional<Customer> existingCustomer = customerRepository.findByNameAndCompanyId(
                            parseResult.getCustomerName(), companyId);

                    if (existingCustomer.isPresent()) {
                        result.setExistingCustomers(result.getExistingCustomers() + 1);
                        pppoeUserToCustomerMap.put(pppoeUser.getId(), existingCustomer.get().getId());
                        log.debug("Cliente já existe: {} (PPPoE: {})",
                                parseResult.getCustomerName(), pppoeUser.getUsername());
                        continue;
                    }

                    // ── 4. Criar novo cliente ──
                    Customer newCustomer = Customer.builder()
                            .companyId(companyId)
                            .name(parseResult.getCustomerName())
                            .type(Customer.CustomerType.FISICA)
                            .document("000.000.000-00") // Atualizar manualmente após sincronização
                            .email(pppoeUser.getEmail() != null ? pppoeUser.getEmail() :
                                   pppoeUser.getUsername() + "@pendente.com")
                            .phonePrimary(parseResult.getPhone())
                            .status(Customer.CustomerStatus.ACTIVE)
                            .notes("Criado via sincronização do MikroTik\n" +
                                   "Login PPPoE: " + pppoeUser.getUsername() + "\n" +
                                   (parseResult.getOriginalComment() != null && !parseResult.getOriginalComment().isBlank()
                                       ? "Localização original: " + parseResult.getOriginalComment()
                                       : ""))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    customerRepository.save(newCustomer);
                    entityManager.flush(); // ⚠️ Garantir ID antes de criar endereço

                    // ✅ Registrar no mapa em memória para Fase 5
                    pppoeUserToCustomerMap.put(pppoeUser.getId(), newCustomer.getId());

                    // ── 5. Endereço de instalação (extraído do comment) ──
                    if (parseResult.getAddress() != null) {
                        Address address = Address.builder()
                                .customerId(newCustomer.getId())
                                .customer(newCustomer)
                                .street(parseResult.getAddress())
                                .number(parseResult.getAddressNumber() != null
                                        ? parseResult.getAddressNumber() : "S/N")
                                .district(parseResult.getNeighborhood())
                                .city("A definir")
                                .state("BA")
                                .zipCode("00000-000")
                                .type(Address.AddressType.INSTALLATION)
                                .createdAt(LocalDateTime.now())
                                .build();

                        addressRepository.save(address);
                        entityManager.flush();
                    }

                    result.setCreatedCustomers(result.getCreatedCustomers() + 1);
                    result.getCreatedCustomerNames().add(newCustomer.getName());

                    log.info("✅ Cliente criado: {} (ID: {}) | PPPoE: {} | Endereço: {}",
                            newCustomer.getName(), newCustomer.getId(),
                            pppoeUser.getUsername(), parseResult.getAddress());

                } catch (Exception e) {
                    log.error("Erro ao criar cliente para PPPoE {}: {}", pppoeUser.getUsername(), e.getMessage());
                    result.getErrorMessages().add("Erro ao criar cliente para "
                            + pppoeUser.getUsername() + ": " + e.getMessage());
                }
            }

            log.info("Clientes: {} criados, {} já existiam | Mapa fase-4→5: {} entradas",
                    result.getCreatedCustomers(), result.getExistingCustomers(),
                    pppoeUserToCustomerMap.size());

        } catch (Exception e) {
            log.error("Erro ao criar clientes: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro na fase de criação de clientes: " + e.getMessage());
        }
    }

    /**
     * FASE 5: Criar Contratos vinculando Cliente + ServicePlan + PPPoE User
     * <p>
     * Usa o pppoeUserToCustomerMap preenchido na Fase 4 para localizar o cliente
     * de cada PPPoE sem depender de escrita no campo comment (hack removido).
     */
    private void createContractsPhase(FullSyncConfigDTO config, FullSyncResultDTO result,
                                      Long companyId, Map<Long, Long> pppoeUserToCustomerMap) {
        try {
            List<PppoeUser> allPppoeUsers = pppoeUserService.findAll();

            for (PppoeUser pppoeUser : allPppoeUsers) {
                try {
                    // Verificar se já existe contrato para este PPPoE
                    Optional<Contract> existingContract = contractRepository.findByPppoeUserIdAndCompanyId(
                            pppoeUser.getId(), companyId);

                    if (existingContract.isPresent()) {
                        log.debug("Contrato já existe para PPPoE: {}", pppoeUser.getUsername());
                        continue;
                    }

                    // ── Localizar cliente via mapa preenchido na Fase 4 ──
                    Long customerId = pppoeUserToCustomerMap.get(pppoeUser.getId());

                    if (customerId == null) {
                        // Fallback: tentar pelo nome derivado do username
                        CustomerInfoParseResult parseResult = parseCustomerInfo(pppoeUser);
                        Optional<Customer> customer = customerRepository.findByNameAndCompanyId(
                                parseResult.getCustomerName(), companyId);

                        if (customer.isPresent()) {
                            customerId = customer.get().getId();
                            pppoeUserToCustomerMap.put(pppoeUser.getId(), customerId);
                        } else {
                            result.getWarnings().add("Cliente não encontrado para PPPoE: "
                                    + pppoeUser.getUsername() + " — execute a Fase 4 primeiro");
                            result.setFailedContracts(result.getFailedContracts() + 1);
                            continue;
                        }
                    }

                    // Buscar ServicePlan para o profile deste PPPoE
                    Optional<ServicePlan> servicePlan = servicePlanRepository.findByPppoeProfileIdAndCompanyId(
                            pppoeUser.getProfile().getId(), companyId);

                    if (servicePlan.isEmpty()) {
                        result.getWarnings().add("Plano de serviço não encontrado para profile: " +
                                pppoeUser.getProfile().getName());
                        result.setFailedContracts(result.getFailedContracts() + 1);
                        continue;
                    }

                    // Buscar endereço de instalação do cliente
                    List<Address> addresses = addressRepository.findByCustomerId(customerId);
                    Long installationAddressId = addresses.isEmpty() ? null : addresses.getFirst().getId();

                    // Criar contrato
                    ContractDTO contractDTO = ContractDTO.builder()
                            .customerId(customerId)
                            .servicePlanId(servicePlan.get().getId())
                            .pppoeUserId(pppoeUser.getId())
                            .installationAddressId(installationAddressId)
                            .startDate(LocalDate.now())
                            .billingDay(config.getDefaultBillingDay())
                            .amount(servicePlan.get().getPrice())
                            .status(Contract.ContractStatus.DRAFT)
                            .build();

                    ContractDTO createdContract = contractService.create(contractDTO);

                    // ⚠️ EXCEÇÃO: PPPoE com profile BLOQUEADO → Suspender financeiramente
                    String profileName = pppoeUser.getProfile().getName();
                    boolean isBlocked = isBlockedProfile(profileName);

                    // Ativar ou suspender contrato conforme profile
                    if (config.getAutoActivateContracts()) {
                        if (isBlocked) {
                            // Profile BLOQUEADO: Criar contrato SUSPENSO
                            contractService.suspendFinancial(createdContract.getId());
                            result.setSuspendedContracts(result.getSuspendedContracts() + 1);
                            log.warn("⚠️ Contrato {} criado SUSPENSO - Profile BLOQUEADO: {} | PPPoE: {}",
                                    createdContract.getId(), profileName, pppoeUser.getUsername());
                        } else {
                            // Profile normal: Ativar contrato
                            contractService.activate(createdContract.getId());
                            result.setActivatedContracts(result.getActivatedContracts() + 1);
                        }
                    }

                    result.setCreatedContracts(result.getCreatedContracts() + 1);
                    result.getCreatedContractIds().add(createdContract.getId().toString());

                    log.info("Contrato criado: ID {} para cliente {} | PPPoE: {} | Plano: {} | Status: {}",
                            createdContract.getId(), customerId, pppoeUser.getUsername(),
                            servicePlan.get().getName(), isBlocked ? "SUSPENDED_FINANCIAL" : "ACTIVE");

                } catch (Exception e) {
                    log.error("Erro ao criar contrato para PPPoE {}: {}", pppoeUser.getUsername(), e.getMessage());
                    result.getErrorMessages().add("Erro ao criar contrato para " + pppoeUser.getUsername() + ": " + e.getMessage());
                    result.setFailedContracts(result.getFailedContracts() + 1);
                }
            }

            log.info("Contratos: {} criados, {} ativados, {} suspensos (profile BLOQUEADO), {} falharam",
                    result.getCreatedContracts(), result.getActivatedContracts(),
                    result.getSuspendedContracts(), result.getFailedContracts());

        } catch (Exception e) {
            log.error("Erro ao criar contratos: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro na fase de criação de contratos: " + e.getMessage());
        }
    }

    /**
     * Parse inteligente das informações de um usuário PPPoE do MikroTik.
     * <p>
     * ESTRATÉGIA (baseada nos dados reais observados):
     * <pre>
     *  Campo "Name" (username) no MikroTik  → NOME DO CLIENTE
     *    ex: "elianabatista"   → "Eliana Batista"
     *    ex: "emersonmoura"    → "Emerson Moura"
     *    ex: "eva"             → "Eva"
     *
     *  Campo "Comment" no MikroTik          → ENDEREÇO / LOCALIZAÇÃO
     *    ex: "rua 1 n120"                   → rua: "Rua 1", número: "120"
     *    ex: "manoel b paixa n63"            → rua: "Manoel B Paixa", número: "63"
     *    ex: "rua po?oes n90 pg"             → rua: "Rua Po?oes", número: "90", bairro: "Pg"
     *    ex: "Rua Dr Alterives Marciel, 135 Bairro Bela Vista - CPF 95266413587"
     *                                        → rua: "Rua Dr Alterives Marciel", número: "135",
     *                                           bairro: "Bela Vista", CPF ignorado
     *    ex: "dene"                          → rua: "Dene" (referência de localização)
     *    ex: vazio / null                    → sem endereço
     * </pre>
     */
    private CustomerInfoParseResult parseCustomerInfo(PppoeUser pppoeUser) {
        String username = pppoeUser.getUsername();
        String comment  = pppoeUser.getComment();

        // ── Nome do cliente: derivado do username ──
        String customerName = splitCamelCaseUsername(username);

        CustomerInfoParseResult result = CustomerInfoParseResult.builder()
                .customerName(customerName)
                .originalComment(comment)
                .parseSuccess(true)
                .build();

        // Sem comentário: apenas nome, sem endereço
        if (comment == null || comment.isBlank()) {
            result.setWarningMessage("PPPoE '" + username + "': sem comentário — endereço não preenchido");
            return result;
        }

        // Remover marcadores legados de CUSTOMER_ID (caso existam de versões anteriores)
        String rawComment = comment.replaceAll("\\[CUSTOMER_ID:\\d+]", "").trim();

        // ── Extrair telefone (padrão: 8 a 11 dígitos isolados) ──
        Pattern phonePattern = Pattern.compile("\\b(\\d{8,11})\\b");
        Matcher phoneMatcher  = phonePattern.matcher(rawComment);
        String phone = null;
        if (phoneMatcher.find()) {
            // Verificar se não parece CPF (11 dígitos contíguos com contexto de CPF)
            boolean isCpf = rawComment.toUpperCase().contains("CPF");
            if (!isCpf || phoneMatcher.group(1).length() != 11) {
                phone = phoneMatcher.group(1);
            }
        }

        // Remover CPF do texto de endereço (ex: "- CPF 95266413587")
        String cleanedComment = rawComment.replaceAll("[-–]?\\s*CPF\\s+\\d+", "").trim();

        // ── Extrair número do imóvel: padrão "n<dígitos>", ",<espaço><dígitos>" ──
        String addressNumber = null;
        Pattern numPattern = Pattern.compile("\\bn\\s*(\\d+)|,\\s*(\\d+)(?=\\s|$)", Pattern.CASE_INSENSITIVE);
        Matcher numMatcher  = numPattern.matcher(cleanedComment);
        if (numMatcher.find()) {
            addressNumber = numMatcher.group(1) != null ? numMatcher.group(1) : numMatcher.group(2);
            // Remover o trecho do número do texto de rua
            cleanedComment = cleanedComment.substring(0, numMatcher.start()).trim();
        }

        // ── Extrair bairro: após "bairro" ou "pg" isolado ao final ──
        String neighborhood = null;
        Pattern bairroPattern = Pattern.compile("\\bbairro\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher bairroMatcher  = bairroPattern.matcher(cleanedComment);
        if (bairroMatcher.find()) {
            neighborhood   = capitalizeWords(bairroMatcher.group(1).trim());
            cleanedComment = cleanedComment.substring(0, bairroMatcher.start()).trim();
        } else {
            // Sufixo de bairro abreviado ao final (ex: "pg", "cj")
            Pattern suffixPattern = Pattern.compile("\\s+(\\w{1,4})$", Pattern.CASE_INSENSITIVE);
            Matcher suffixMatcher  = suffixPattern.matcher(cleanedComment);
            if (suffixMatcher.find()) {
                String candidate = suffixMatcher.group(1);
                // Só trata como bairro se for palavra curta não-numérica após o número já removido
                if (candidate.matches("[a-zA-Z]{1,4}") && addressNumber != null) {
                    neighborhood   = capitalizeWords(candidate);
                    cleanedComment = cleanedComment.substring(0, suffixMatcher.start()).trim();
                }
            }
        }

        // ── Rua/localização: tudo que restou no comment limpo ──
        String street = cleanedComment.isEmpty() ? null : capitalizeWords(cleanedComment);

        result.setAddress(street);
        result.setAddressNumber(addressNumber);
        result.setNeighborhood(neighborhood);
        result.setPhone(phone);

        return result;
    }

    /**
     * Converte um username sem espaços (camelCase ou tudo junto) em nome capitalizado.
     * <p>
     * Estratégia:
     * - Se já contém espaço → apenas capitaliza as palavras.
     * - Se parece camelCase (letra maiúscula no meio) → divide e capitaliza.
     * - Se tudo minúsculo sem separador (caso mais comum: "elianabatista") →
     *   não há como saber os limites sem dicionário; capitaliza apenas a 1ª letra
     *   e deixa para o operador corrigir depois (comportamento seguro e não-destrutivo).
     * <p>
     * Exemplos:
     *   "elianabatista"         → "Elianabatista"  (operador corrige depois)
     *   "emersonmoura"          → "Emersonmoura"
     *   "elisangelasilvalima"   → "Elisangelasilvalima"
     *   "eva"                   → "Eva"
     *   "fabioRamos"            → "Fabio Ramos"    (camelCase detectado)
     *   "fabio ramos"           → "Fabio Ramos"    (já tem espaço)
     */
    private String splitCamelCaseUsername(String username) {
        if (username == null || username.isBlank()) {
            return "Desconhecido";
        }

        // Já tem espaço: apenas capitaliza
        if (username.contains(" ")) {
            return capitalizeWords(username);
        }

        // CamelCase: divide em palavras onde maiúscula aparece
        // ex: "fabioRamos" → "Fabio Ramos"
        String camelSplit = username.replaceAll("([a-z])([A-Z])", "$1 $2");
        if (!camelSplit.equals(username)) {
            return capitalizeWords(camelSplit);
        }

        // Tudo junto minúsculo: capitaliza apenas a 1ª letra (seguro)
        return Character.toUpperCase(username.charAt(0)) + username.substring(1).toLowerCase();
    }

    /**
     * Capitaliza palavras de uma string
     */
    private String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String[] words = text.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Verifica se o profile é do tipo "BLOQUEADO" (case-insensitive).
     * ⚠️ CRÍTICO: Profiles bloqueados devem gerar contratos SUSPENDED_FINANCIAL.
     * Também detecta variantes que contenham "bloqueado" (ex: "PLANO-BLOQUEADO").
     */
    private boolean isBlockedProfile(String profileName) {
        if (profileName == null) {
            return false;
        }
        return profileName.trim().toUpperCase().contains("BLOQUEADO");
    }
}

