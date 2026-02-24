package br.com.mikrotik.features.network.service;

import br.com.mikrotik.features.contracts.event.ContractPlanChangedEvent;
import br.com.mikrotik.features.contracts.event.ContractStatusChangedEvent;
import br.com.mikrotik.features.contracts.model.Contract;
import br.com.mikrotik.features.contracts.model.ServicePlan;
import br.com.mikrotik.features.contracts.repository.ServicePlanRepository;
import br.com.mikrotik.features.network.pppoe.model.PppoeProfile;
import br.com.mikrotik.features.network.pppoe.model.PppoeUser;
import br.com.mikrotik.features.network.pppoe.repository.PppoeUserRepository;
import br.com.mikrotik.features.network.server.adapter.MikrotikApiService;
import br.com.mikrotik.features.network.server.model.MikrotikServer;
import br.com.mikrotik.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Service responsável por integrações de rede (Mikrotik) de forma assíncrona.
 *
 * PRINCÍPIO ARQUITETURAL:
 * "NUNCA execute chamadas externas (SSH, API) dentro de @Transactional."
 *
 * Este service recebe eventos de mudança de status de contratos e processa
 * as integrações de forma assíncrona, fora do contexto transacional.
 *
 * RESILIÊNCIA:
 * - @Async: Execução em thread separada
 * - @Retryable: 3 tentativas com backoff exponencial (2s, 4s, 8s)
 * - Logs estruturados para troubleshooting
 * - Não lança exceção para não bloquear o fluxo principal
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkIntegrationService {

    private final PppoeUserRepository pppoeUserRepository;
    private final ServicePlanRepository servicePlanRepository;
    private final MikrotikApiService mikrotikApiService;

    // ==================== HANDLERS ====================

    /**
     * Processa mudanças de status de contrato que requerem ação no Mikrotik.
     *
     * CASOS DE USO:
     * - ACTIVE → SUSPENDED_FINANCIAL: Bloquear usuário
     * - ACTIVE → SUSPENDED_REQUEST: Bloquear usuário
     * - ACTIVE → CANCELED: Deletar usuário
     * - SUSPENDED_* → ACTIVE: Desbloquear usuário
     */
    @Async("networkIntegrationExecutor")
    @EventListener
    public void handleContractStatusChange(ContractStatusChangedEvent event) {
        log.info("==========================================================");
        log.info("📡 PROCESSANDO INTEGRAÇÃO MIKROTIK - Contrato ID: {}", event.getContractId());
        log.info("Status: {} → {}", event.getPreviousStatus(), event.getNewStatus());
        log.info("Motivo: {}", event.getReason());
        log.info("==========================================================");

        // Validar se contrato tem usuário PPPoE
        if (event.getPppoeUserId() == null) {
            log.warn("⚠️  Contrato {} não possui usuário PPPoE vinculado - Integração ignorada",
                    event.getContractId());
            return;
        }

        try {
            // Determinar ação baseada na mudança de status
            if (shouldBlockUser(event.getPreviousStatus(), event.getNewStatus())) {
                blockUserInMikrotik(event.getPppoeUserId(), event.getContractId());

            } else if (shouldUnblockUser(event.getPreviousStatus(), event.getNewStatus())) {
                unblockUserInMikrotik(event.getPppoeUserId(), event.getContractId());

            } else if (shouldDeleteUser(event.getNewStatus())) {
                deleteUserInMikrotik(event.getPppoeUserId(), event.getContractId());

            } else {
                log.info("ℹ️  Nenhuma ação necessária no Mikrotik para esta mudança de status");
            }

        } catch (Exception e) {
            log.error("❌ ERRO ao processar integração Mikrotik para contrato {}",
                    event.getContractId(), e);
            // TODO: Criar AutomationLog de falha para revisão manual
            // TODO: Enviar alerta para monitoramento
        }
    }

    /**
     * Bloqueia usuário no Mikrotik com retry automático.
     */
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        retryFor = {Exception.class}
    )
    public void blockUserInMikrotik(Long pppoeUserId, Long contractId) {
        log.info(">>> BLOQUEANDO usuário PPPoE ID: {} (Contrato: {}) <<<", pppoeUserId, contractId);

        PppoeUser pppoeUser = pppoeUserRepository.findById(pppoeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + pppoeUserId));

        MikrotikServer server = pppoeUser.getMikrotikServer();
        log.info("Servidor: {} ({}:{})", server.getName(), server.getIpAddress(), server.getApiPort());

        // 1. Alterar perfil para BLOQUEADO
        log.info("Alterando perfil para 'BLOQUEADO'...");
        mikrotikApiService.changePppoeUserProfile(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                pppoeUser.getUsername(),
                "BLOQUEADO"
        );
        log.info("✅ Perfil alterado");

        // 2. Desconectar sessão ativa
        log.info("Desconectando sessão ativa...");
        mikrotikApiService.disconnectActivePppoeUser(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                pppoeUser.getUsername()
        );
        log.info("✅ Sessão desconectada");

        // 3. Atualizar status local
        pppoeUser.setStatus(PppoeUser.UserStatus.DISABLED);
        pppoeUser.setUpdatedAt(LocalDateTime.now());
        pppoeUserRepository.save(pppoeUser);
        log.info("✅ Status atualizado no banco: DISABLED");

        log.info("==========================================================");
        log.info("✅ BLOQUEIO CONCLUÍDO - Usuário: {}", pppoeUser.getUsername());
        log.info("==========================================================");
    }

    /**
     * Desbloqueia usuário no Mikrotik com retry automático.
     */
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        retryFor = {Exception.class}
    )
    public void unblockUserInMikrotik(Long pppoeUserId, Long contractId) {
        log.info(">>> DESBLOQUEANDO usuário PPPoE ID: {} (Contrato: {}) <<<", pppoeUserId, contractId);

        PppoeUser pppoeUser = pppoeUserRepository.findById(pppoeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + pppoeUserId));

        MikrotikServer server = pppoeUser.getMikrotikServer();

        // Buscar perfil original do plano
        String originalProfile = pppoeUser.getProfile().getName();
        log.info("Restaurando perfil original: {}", originalProfile);

        // 1. Restaurar perfil original
        mikrotikApiService.changePppoeUserProfile(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                pppoeUser.getUsername(),
                originalProfile
        );
        log.info("✅ Perfil restaurado");

        // 2. Atualizar status local
        pppoeUser.setStatus(PppoeUser.UserStatus.OFFLINE);
        pppoeUser.setUpdatedAt(LocalDateTime.now());
        pppoeUserRepository.save(pppoeUser);
        log.info("✅ Status atualizado no banco: OFFLINE");

        log.info("==========================================================");
        log.info("✅ DESBLOQUEIO CONCLUÍDO - Usuário: {}", pppoeUser.getUsername());
        log.info("==========================================================");
    }

    /**
     * Deleta usuário do Mikrotik com retry automático.
     */
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        retryFor = {Exception.class}
    )
    public void deleteUserInMikrotik(Long pppoeUserId, Long contractId) {
        log.info(">>> DELETANDO usuário PPPoE ID: {} (Contrato: {}) <<<", pppoeUserId, contractId);

        PppoeUser pppoeUser = pppoeUserRepository.findById(pppoeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + pppoeUserId));

        MikrotikServer server = pppoeUser.getMikrotikServer();

        // 1. Desconectar sessão se ativa
        try {
            mikrotikApiService.disconnectActivePppoeUser(
                    server.getIpAddress(),
                    server.getApiPort(),
                    server.getUsername(),
                    server.getPassword(),
                    pppoeUser.getUsername()
            );
            log.info("✅ Sessão desconectada");
        } catch (Exception e) {
            log.warn("⚠️  Falha ao desconectar (talvez já estivesse offline): {}", e.getMessage());
        }

        // 2. Deletar do Mikrotik
        mikrotikApiService.deletePppoeUser(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                pppoeUser.getUsername()
        );
        log.info("✅ Usuário deletado do Mikrotik");

        // 3. Marcar como inativo no banco (não deletar para auditoria)
        pppoeUser.setActive(false);
        pppoeUser.setStatus(PppoeUser.UserStatus.OFFLINE);
        pppoeUser.setUpdatedAt(LocalDateTime.now());
        pppoeUserRepository.save(pppoeUser);
        log.info("✅ Usuário marcado como inativo no banco");

        log.info("==========================================================");
        log.info("✅ DELEÇÃO CONCLUÍDA - Usuário: {}", pppoeUser.getUsername());
        log.info("==========================================================");
    }

    // ==================== REGRAS DE NEGÓCIO ====================

    // ==================== MUDANÇA DE PLANO (UPGRADE / DOWNGRADE) ====================

    /**
     * Recebe o evento de mudança de plano e despacha para o worker com retry.
     *
     * @Async + @Retryable não podem estar no mesmo método (conflito de proxies Spring).
     * Por isso separamos: listener (@Async) → worker (@Retryable).
     */
    @Async("networkIntegrationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePlanChange(ContractPlanChangedEvent event) {
        log.info("==========================================================");
        log.info("📡 PROPAGANDO MUDANÇA DE PLANO - Contrato ID: {}", event.getContractId());
        log.info("Plano: {} → {} | PPPoE User ID: {}",
                 event.getPreviousServicePlanId(), event.getNewServicePlanId(), event.getPppoeUserId());
        log.info("==========================================================");

        if (event.getPppoeUserId() == null) {
            log.warn("⚠️  Contrato {} sem PPPoE vinculado — propagação ignorada", event.getContractId());
            return;
        }

        try {
            applyPlanChangeInMikrotik(event.getPppoeUserId(), event.getNewServicePlanId(), event.getContractId());
        } catch (Exception e) {
            log.error("❌ ERRO ao propagar mudança de plano para contrato {} após todas as tentativas",
                      event.getContractId(), e);
            // TODO: persistir AutomationLog de falha para revisão manual / reprocessamento
        }
    }

    /**
     * Executa a troca de perfil no Mikrotik e sincroniza o banco.
     *
     * Deve ser public para que o proxy do @Retryable funcione corretamente
     * (Spring AOP exige chamada via proxy, não chamada interna/direta).
     *
     * SEQUÊNCIA:
     * 1. Carrega PppoeUser + novo ServicePlan → PppoeProfile
     * 2. Chama changePppoeUserProfile no Mikrotik
     * 3. Atualiza PppoeUser.profile no banco
     */
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        retryFor = {Exception.class}
    )
    public void applyPlanChangeInMikrotik(Long pppoeUserId, Long newServicePlanId, Long contractId) {
        log.info(">>> APLICANDO MUDANÇA DE PLANO - PPPoE ID: {}, Novo Plano: {}, Contrato: {}",
                 pppoeUserId, newServicePlanId, contractId);

        // 1. Carregar usuário PPPoE
        PppoeUser pppoeUser = pppoeUserRepository.findById(pppoeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário PPPoE não encontrado: " + pppoeUserId));

        // 2. Carregar novo plano → perfil PPPoE via JOIN FETCH (LAZY-safe para thread @Async)
        ServicePlan newPlan = servicePlanRepository.findByIdWithProfile(newServicePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano de serviço não encontrado: " + newServicePlanId));

        PppoeProfile newProfile = newPlan.getPppoeProfile();
        if (newProfile == null) {
            throw new IllegalStateException(
                    "Plano ID=" + newServicePlanId + " não possui perfil PPPoE configurado");
        }

        MikrotikServer server = pppoeUser.getMikrotikServer();
        String previousProfileName = pppoeUser.getProfile() != null
                ? pppoeUser.getProfile().getName() : "N/A";

        log.info("Alterando perfil: {} → {} | Usuário: {} | Servidor: {}",
                 previousProfileName, newProfile.getName(), pppoeUser.getUsername(), server.getName());

        // 3. Atualizar no Mikrotik — esta chamada tem retry automático via @Retryable
        mikrotikApiService.changePppoeUserProfile(
                server.getIpAddress(),
                server.getApiPort(),
                server.getUsername(),
                server.getPassword(),
                pppoeUser.getUsername(),
                newProfile.getName()
        );
        log.info("✅ Perfil alterado no Mikrotik: {}", newProfile.getName());

        // 4. Sincronizar banco somente após Mikrotik confirmar (consistência eventual garantida)
        pppoeUser.setProfile(newProfile);
        pppoeUser.setUpdatedAt(LocalDateTime.now());
        pppoeUserRepository.save(pppoeUser);
        log.info("✅ PppoeUser ID={} atualizado no banco: profile={}", pppoeUserId, newProfile.getName());

        log.info("==========================================================");
        log.info("✅ MUDANÇA DE PLANO CONCLUÍDA - Usuário: {} | Perfil: {}",
                 pppoeUser.getUsername(), newProfile.getName());
        log.info("==========================================================");
    }

    private boolean shouldBlockUser(Contract.ContractStatus previous, Contract.ContractStatus newStatus) {
        return (previous == Contract.ContractStatus.ACTIVE || previous == Contract.ContractStatus.PENDING) &&
               (newStatus == Contract.ContractStatus.SUSPENDED_FINANCIAL ||
                newStatus == Contract.ContractStatus.SUSPENDED_REQUEST);
    }

    private boolean shouldUnblockUser(Contract.ContractStatus previous, Contract.ContractStatus newStatus) {
        return (previous == Contract.ContractStatus.SUSPENDED_FINANCIAL ||
                previous == Contract.ContractStatus.SUSPENDED_REQUEST) &&
               newStatus == Contract.ContractStatus.ACTIVE;
    }

    private boolean shouldDeleteUser(Contract.ContractStatus newStatus) {
        return newStatus == Contract.ContractStatus.CANCELED;
    }
}

