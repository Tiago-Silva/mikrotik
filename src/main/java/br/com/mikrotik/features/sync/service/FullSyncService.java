package br.com.mikrotik.features.sync.service;

import br.com.mikrotik.features.sync.dto.FullSyncConfigDTO;
import br.com.mikrotik.features.sync.dto.FullSyncResultDTO;
import br.com.mikrotik.features.sync.dto.SyncResultDTO;
import br.com.mikrotik.features.sync.dto.CustomerInfoParseResult;
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
import java.util.List;
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
                createCustomersPhase(result, companyId);

                // ⚠️ CRÍTICO: Flush e clear após criar clientes
                entityManager.flush();
                entityManager.clear();
            }

            // FASE 5: Criar Contratos
            if (config.getCreateContracts()) {
                log.info("\n--- FASE 5: Criando Contratos ---");
                createContractsPhase(config, result, companyId);

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
        log.info("Contratos: {} criados, {} ativados", result.getCreatedContracts(), result.getActivatedContracts());
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
     * FASE 4: Criar Clientes a partir dos comentários dos usuários PPPoE
     */
    private void createCustomersPhase(FullSyncResultDTO result, Long companyId) {
        try {
            List<PppoeUser> allPppoeUsers = pppoeUserService.findAll();

            for (PppoeUser pppoeUser : allPppoeUsers) {
                try {
                    // Verificar se já existe cliente vinculado a este usuário PPPoE
                    Optional<Contract> existingContract = contractRepository.findByPppoeUserIdAndCompanyId(
                            pppoeUser.getId(), companyId);

                    if (existingContract.isPresent()) {
                        result.setExistingCustomers(result.getExistingCustomers() + 1);
                        log.debug("Cliente já existe para PPPoE: {}", pppoeUser.getUsername());
                        continue;
                    }

                    // Parse informações do comentário
                    CustomerInfoParseResult parseResult = parseCustomerInfo(pppoeUser);

                    if (!parseResult.getParseSuccess() && parseResult.getWarningMessage() != null) {
                        result.getWarnings().add(parseResult.getWarningMessage());
                    }

                    // Verificar se já existe cliente com esse nome
                    Optional<Customer> existingCustomer = customerRepository.findByNameAndCompanyId(
                            parseResult.getCustomerName(), companyId);

                    if (existingCustomer.isPresent()) {
                        result.setExistingCustomers(result.getExistingCustomers() + 1);
                        log.debug("Cliente já existe com nome: {}", parseResult.getCustomerName());

                        // Armazenar para criar contrato depois
                        pppoeUser.setComment(pppoeUser.getComment() + " [CUSTOMER_ID:" + existingCustomer.get().getId() + "]");
                        continue;
                    }

                    // Criar novo cliente
                    Customer newCustomer = Customer.builder()
                            .companyId(companyId)
                            .name(parseResult.getCustomerName())
                            .type(Customer.CustomerType.FISICA)
                            .document("000.000.000-00") // Documento padrão - deve ser atualizado depois
                            .email(pppoeUser.getEmail() != null ? pppoeUser.getEmail() :
                                   pppoeUser.getUsername() + "@pendente.com")
                            .phonePrimary(parseResult.getPhone())
                            .status(Customer.CustomerStatus.ACTIVE)
                            .notes("Cliente criado automaticamente na sincronização do MikroTik\n" +
                                   "PPPoE Username: " + pppoeUser.getUsername() + "\n" +
                                   "Comentário original: " + parseResult.getOriginalComment())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    customerRepository.save(newCustomer);
                    entityManager.flush(); // ⚠️ CRÍTICO: Flush para garantir ID gerado

                    // Criar endereço se tiver informações
                    if (parseResult.getAddress() != null) {
                        Address address = Address.builder()
                                .customerId(newCustomer.getId())
                                .customer(newCustomer)
                                .street(parseResult.getAddress())
                                .number(parseResult.getAddressNumber() != null ? parseResult.getAddressNumber() : "S/N")
                                .district(parseResult.getNeighborhood())
                                .city("A definir")
                                .state("BA")
                                .zipCode("00000-000")
                                .type(Address.AddressType.INSTALLATION)
                                .createdAt(LocalDateTime.now())
                                .build();

                        addressRepository.save(address);
                        entityManager.flush(); // Flush endereço também
                    }

                    result.setCreatedCustomers(result.getCreatedCustomers() + 1);
                    result.getCreatedCustomerNames().add(newCustomer.getName());


                    log.info("Cliente criado: {} (ID: {}) para PPPoE: {}",
                            newCustomer.getName(), newCustomer.getId(), pppoeUser.getUsername());

                } catch (Exception e) {
                    log.error("Erro ao criar cliente para PPPoE {}: {}", pppoeUser.getUsername(), e.getMessage());
                    result.getErrorMessages().add("Erro ao criar cliente para " + pppoeUser.getUsername() + ": " + e.getMessage());
                }
            }

            log.info("Clientes: {} criados, {} já existiam", result.getCreatedCustomers(), result.getExistingCustomers());

        } catch (Exception e) {
            log.error("Erro ao criar clientes: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro na fase de criação de clientes: " + e.getMessage());
        }
    }

    /**
     * FASE 5: Criar Contratos vinculando Cliente + ServicePlan + PPPoE User
     */
    private void createContractsPhase(FullSyncConfigDTO config, FullSyncResultDTO result, Long companyId) {
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

                    // Buscar ou extrair ID do cliente do comentário
                    Long customerId = extractCustomerIdFromComment(pppoeUser.getComment());

                    if (customerId == null) {
                        // Tentar buscar cliente pelo nome parseado
                        CustomerInfoParseResult parseResult = parseCustomerInfo(pppoeUser);
                        Optional<Customer> customer = customerRepository.findByNameAndCompanyId(
                                parseResult.getCustomerName(), companyId);

                        if (customer.isPresent()) {
                            customerId = customer.get().getId();
                        } else {
                            result.getWarnings().add("Cliente não encontrado para PPPoE: " + pppoeUser.getUsername());
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

                    // Ativar contrato se configurado
                    if (config.getAutoActivateContracts()) {
                        contractService.activate(createdContract.getId());
                        result.setActivatedContracts(result.getActivatedContracts() + 1);
                    }

                    result.setCreatedContracts(result.getCreatedContracts() + 1);
                    result.getCreatedContractIds().add(createdContract.getId().toString());

                    log.info("Contrato criado: ID {} para cliente {} | PPPoE: {} | Plano: {}",
                            createdContract.getId(), customerId, pppoeUser.getUsername(), servicePlan.get().getName());

                } catch (Exception e) {
                    log.error("Erro ao criar contrato para PPPoE {}: {}", pppoeUser.getUsername(), e.getMessage());
                    result.getErrorMessages().add("Erro ao criar contrato para " + pppoeUser.getUsername() + ": " + e.getMessage());
                    result.setFailedContracts(result.getFailedContracts() + 1);
                }
            }

            log.info("Contratos: {} criados, {} ativados, {} falharam",
                    result.getCreatedContracts(), result.getActivatedContracts(), result.getFailedContracts());

        } catch (Exception e) {
            log.error("Erro ao criar contratos: {}", e.getMessage(), e);
            result.getErrorMessages().add("Erro na fase de criação de contratos: " + e.getMessage());
        }
    }

    /**
     * Parse inteligente de informações do comentário PPPoE
     */
    private CustomerInfoParseResult parseCustomerInfo(PppoeUser pppoeUser) {
        String comment = pppoeUser.getComment();
        String username = pppoeUser.getUsername();

        CustomerInfoParseResult result = CustomerInfoParseResult.builder()
                .originalComment(comment)
                .parseSuccess(false)
                .build();

        // Se não tem comentário, usa o username como nome
        if (comment == null || comment.trim().isEmpty() || comment.equals("Sincronizado do Mikrotik")) {
            result.setCustomerName(capitalizeWords(username));
            result.setParseSuccess(true);
            result.setWarningMessage("PPPoE " + username + ": sem comentário, usando username como nome");
            return result;
        }

        // Remover marcadores de CUSTOMER_ID se existir
        comment = comment.replaceAll("\\[CUSTOMER_ID:\\d+]", "").trim();

        // Patterns para extração
        // Exemplo: "felipe achy/ nalmar alcantara n255/ ana carolina"
        // Exemplo: "rua7"
        // Exemplo: "travessa o mangabeira n214"

        String customerName = null;
        String address = null;
        String addressNumber = null;
        String neighborhood = null;

        // Tentar extrair nome (primeira parte antes de / ou n<número>)
        Pattern namePattern = Pattern.compile("^([^/n]+?)(?:/|n\\d|$)", Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(comment);
        if (nameMatcher.find()) {
            customerName = nameMatcher.group(1).trim();
        }

        // Tentar extrair endereço (rua, travessa, avenida, etc)
        Pattern addressPattern = Pattern.compile("(rua|travessa|avenida|av|trav)\\s+([^/n]+)", Pattern.CASE_INSENSITIVE);
        Matcher addressMatcher = addressPattern.matcher(comment);
        if (addressMatcher.find()) {
            address = addressMatcher.group(1) + " " + addressMatcher.group(2).trim();
        }

        // Tentar extrair número
        Pattern numberPattern = Pattern.compile("\\bn\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher numberMatcher = numberPattern.matcher(comment);
        if (numberMatcher.find()) {
            addressNumber = numberMatcher.group(1);
        }

        // Se não conseguiu extrair nome, usa username
        if (customerName == null || customerName.isEmpty()) {
            customerName = capitalizeWords(username);
            result.setWarningMessage("PPPoE " + username + ": não foi possível extrair nome do comentário");
        } else {
            customerName = capitalizeWords(customerName);
        }

        result.setCustomerName(customerName);
        result.setAddress(address);
        result.setAddressNumber(addressNumber);
        result.setNeighborhood(neighborhood);
        result.setParseSuccess(true);

        return result;
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
     * Extrai ID do cliente do comentário no formato [CUSTOMER_ID:123]
     */
    private Long extractCustomerIdFromComment(String comment) {
        if (comment == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\\[CUSTOMER_ID:(\\d+)]");
        Matcher matcher = pattern.matcher(comment);

        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        return null;
    }
}

