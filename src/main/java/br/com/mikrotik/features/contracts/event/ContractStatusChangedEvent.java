package br.com.mikrotik.features.contracts.event;

import br.com.mikrotik.features.contracts.model.Contract;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento publicado quando o status de um contrato muda.
 *
 * ARQUITETURA:
 * Este evento desacopla a lógica transacional (mudança de status no banco)
 * da integração externa (bloqueio/desbloqueio no Mikrotik).
 *
 * FLUXO:
 * 1. ContractService.suspendFinancial() -> Altera status no DB (@Transactional)
 * 2. Publica ContractStatusChangedEvent (dentro da transação)
 * 3. Commit da transação
 * 4. NetworkIntegrationService recebe evento (@Async)
 * 5. Processa integração Mikrotik (fora da transação, com retry)
 */
@Getter
public class ContractStatusChangedEvent extends ApplicationEvent {

    private final Long contractId;
    private final Long companyId;
    private final Contract.ContractStatus previousStatus;
    private final Contract.ContractStatus newStatus;
    private final Long pppoeUserId;
    private final String reason;

    public ContractStatusChangedEvent(Object source, Long contractId, Long companyId,
                                     Contract.ContractStatus previousStatus,
                                     Contract.ContractStatus newStatus,
                                     Long pppoeUserId,
                                     String reason) {
        super(source);
        this.contractId = contractId;
        this.companyId = companyId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.pppoeUserId = pppoeUserId;
        this.reason = reason;
    }
}

