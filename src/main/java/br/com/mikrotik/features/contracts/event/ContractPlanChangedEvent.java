package br.com.mikrotik.features.contracts.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado quando o plano de serviço de um contrato é alterado (upgrade/downgrade).
 *
 * ARQUITETURA:
 * Desacopla a atualização transacional do banco (servicePlanId no contrato)
 * da integração assíncrona com o Mikrotik (changePppoeUserProfile).
 *
 * REGRA DE OURO: Chamadas externas ao Mikrotik NUNCA dentro de @Transactional.
 *
 * FLUXO:
 * 1. ContractService.update() detecta mudança de servicePlanId
 * 2. Persiste novo servicePlanId no contrato (@Transactional) → commit
 * 3. Publica ContractPlanChangedEvent (após save, antes de retornar)
 * 4. NetworkIntegrationService.handlePlanChange() recebe evento (@Async)
 * 5. Carrega novo ServicePlan → PppoeProfile
 * 6. Atualiza PppoeUser.profile no banco
 * 7. Chama changePppoeUserProfile no Mikrotik (com @Retryable 3x)
 *
 * CONSISTÊNCIA:
 * - Contrato sempre reflete o plano correto no banco
 * - PppoeUser.profile é atualizado de forma assíncrona
 * - Mikrotik é atualizado de forma assíncrona com retry
 * - Se o Mikrotik falhar após 3 tentativas, log de erro para revisão manual
 */
@Getter
public class ContractPlanChangedEvent extends ApplicationEvent {

    private final Long contractId;
    private final Long companyId;
    private final Long pppoeUserId;
    private final Long previousServicePlanId;
    private final Long newServicePlanId;

    public ContractPlanChangedEvent(Object source,
                                    Long contractId,
                                    Long companyId,
                                    Long pppoeUserId,
                                    Long previousServicePlanId,
                                    Long newServicePlanId) {
        super(source);
        this.contractId            = contractId;
        this.companyId             = companyId;
        this.pppoeUserId           = pppoeUserId;
        this.previousServicePlanId = previousServicePlanId;
        this.newServicePlanId      = newServicePlanId;
    }
}

