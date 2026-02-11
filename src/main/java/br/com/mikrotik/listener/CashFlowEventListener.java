package br.com.mikrotik.listener;

import br.com.mikrotik.event.InvoicePaidEvent;
import br.com.mikrotik.service.CashFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener para processar eventos de pagamento de faturas
 * Event-Driven Architecture: Desacopla faturamento de fluxo de caixa
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CashFlowEventListener {

    private final CashFlowService cashFlowService;

    /**
     * Captura evento de fatura paga e cria lançamento financeiro automaticamente
     *
     * @Async: Executa de forma assíncrona para não travar o processo de pagamento
     * @Transactional(propagation = REQUIRES_NEW): Cria nova transação independente
     */
    @EventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onInvoicePaid(InvoicePaidEvent event) {
        try {
            log.info("==========================================================");
            log.info(">>> EVENTO CAPTURADO: InvoicePaidEvent <<<");
            log.info("Invoice ID: {}", event.getInvoiceId());
            log.info("Company ID: {}", event.getCompanyId());
            log.info("Amount: {}", event.getAmountPaid());
            log.info("Payment Method: {}", event.getPaymentMethod());
            log.info("==========================================================");

            // Processar pagamento no fluxo de caixa
            cashFlowService.processInvoicePayment(
                    event.getInvoiceId(),
                    event.getCompanyId(),
                    event.getAmountPaid(),
                    event.getPaidAt()
            );

            log.info("==========================================================");
            log.info("✅ LANÇAMENTO FINANCEIRO CRIADO COM SUCESSO");
            log.info("==========================================================");

        } catch (Exception e) {
            log.error("==========================================================");
            log.error("❌ ERRO AO PROCESSAR PAGAMENTO NO FLUXO DE CAIXA");
            log.error("Invoice ID: {}", event.getInvoiceId());
            log.error("Erro: {}", e.getMessage(), e);
            log.error("==========================================================");

            // NÃO lança exceção para não impedir que a fatura seja marcada como paga
            // O pagamento já foi registrado na tabela 'transactions'
            // Este é apenas um lançamento contábil adicional
        }
    }
}

