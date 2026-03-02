package br.com.mikrotik.features.invoices.listener;

import br.com.mikrotik.features.invoices.event.InvoiceCreatedEvent;
import br.com.mikrotik.features.invoices.model.Invoice;
import br.com.mikrotik.features.invoices.repository.InvoiceRepository;
import br.com.mikrotik.shared.infrastructure.payment.PaymentGatewayPort;
import br.com.mikrotik.shared.infrastructure.payment.PixChargeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

/**
 * Listener que integra com o gateway de pagamento após a fatura ser persistida.
 *
 * ARQUITETURA (REGRA DE OURO):
 *  - @TransactionalEventListener(phase = AFTER_COMMIT): garante que a fatura já está
 *    visível no banco ANTES de chamar a API externa. Evita inconsistência onde
 *    o AbacatePay confirma mas a fatura ainda não existe.
 *
 *  - @Async: executa em thread separada (pool networkIntegrationExecutor).
 *    O endpoint POST /billing/generate retorna 201 imediatamente, sem esperar
 *    a resposta do AbacatePay.
 *
 *  - @Transactional(REQUIRES_NEW): abre uma nova transação apenas para gravar
 *    paymentLink/externalId. Falha aqui não reverte a fatura já criada.
 *
 *  - Fail-safe: se o AbacatePay falhar, a fatura permanece PENDING sem link.
 *    Aceitável — o ISP pode reprocessar manualmente ou via job de reconciliação.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AbacatePayEventListener {

    private final InvoiceRepository invoiceRepository;
    private final PaymentGatewayPort paymentGateway;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("networkIntegrationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInvoiceCreated(InvoiceCreatedEvent event) {
        log.info("[AbacatePay] Processando InvoiceCreatedEvent — fatura #{}, empresa #{}",
                event.invoiceId(), event.companyId());

        Invoice invoice = invoiceRepository.findById(event.invoiceId()).orElse(null);
        if (invoice == null) {
            log.error("[AbacatePay] Fatura #{} não encontrada no banco. Evento ignorado.", event.invoiceId());
            return;
        }

        // Carrega relacionamento do customer necessário para o payload do gateway
        // (LAZY — já estamos fora da transação original, nova transação aberta aqui)
        Optional<PixChargeResult> result = paymentGateway.generatePixCharge(invoice);

        result.ifPresentOrElse(
                charge -> {
                    invoice.setExternalId(charge.externalId());
                    invoice.setPaymentLink(charge.paymentLink());
                    invoiceRepository.save(invoice);
                    log.info("[AbacatePay] Link PIX gravado — fatura #{} externalId={}",
                            invoice.getId(), charge.externalId());
                },
                () -> log.warn("[AbacatePay] Gateway não retornou link. " +
                               "Fatura #{} permanece PENDING sem paymentLink.", invoice.getId())
        );
    }
}

