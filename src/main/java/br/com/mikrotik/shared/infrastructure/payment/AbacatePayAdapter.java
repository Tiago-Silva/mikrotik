package br.com.mikrotik.shared.infrastructure.payment;

import br.com.mikrotik.features.invoices.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Optional;

/**
 * Adapter para a API AbacatePay.
 *
 * Documentação AbacatePay: https://abacatepay.readme.io/reference
 *
 * TRADE-OFF: Usamos RestClient (Spring 6 / Boot 3) síncrono propositalmente.
 * Esta chamada já é executada fora de qualquer transação (via @TransactionalEventListener
 * AFTER_COMMIT + @Async). Adicionar reatividade aqui seria over-engineering.
 *
 * Se abacatepay.enabled=false (ex: ambiente de dev sem credenciais), o método
 * retorna Optional.empty() sem lançar exceção — a fatura fica PENDING sem link,
 * o que é aceitável e reversível.
 */
@Component
@Slf4j
public class AbacatePayAdapter implements PaymentGatewayPort {

    private final RestClient restClient;

    @Value("${abacatepay.enabled:false}")
    private boolean enabled;

    @Value("${abacatepay.api-key:}")
    private String apiKey;

    public AbacatePayAdapter(
            @Value("${abacatepay.base-url:https://api.abacatepay.com/v1}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Optional<PixChargeResult> generatePixCharge(Invoice invoice) {
        if (!enabled) {
            log.info("[AbacatePay] Integração desabilitada (abacatepay.enabled=false). " +
                     "Fatura #{} ficará sem link de pagamento.", invoice.getId());
            return Optional.empty();
        }

        try {
            log.info("[AbacatePay] Gerando cobrança PIX para fatura #{}  valor=R${}",
                    invoice.getId(), invoice.getFinalAmount());

            // Monta o payload conforme a API AbacatePay
            // Referência: POST /billing/create
            var payload = Map.of(
                    "frequency",   "ONE_TIME",
                    "methods",     new String[]{"PIX"},
                    "returnUrl",   "",
                    "completionUrl", "",
                    "customer",    Map.of(
                            "name",     invoice.getCustomer() != null
                                        ? invoice.getCustomer().getName()
                                        : "Cliente #" + invoice.getCustomerId(),
                            "cellphone", invoice.getCustomer() != null
                                         && invoice.getCustomer().getPhonePrimary() != null
                                         ? invoice.getCustomer().getPhonePrimary() : "",
                            "email",    invoice.getCustomer() != null
                                        && invoice.getCustomer().getEmail() != null
                                        ? invoice.getCustomer().getEmail() : "",
                            "taxId",    invoice.getCustomer() != null
                                        ? invoice.getCustomer().getDocument() : ""
                    ),
                    "products",    new Object[]{Map.of(
                            "externalId", "INV-" + invoice.getId(),
                            "name",       invoice.getDescription() != null
                                          ? invoice.getDescription() : "Mensalidade Internet",
                            "quantity",   1,
                            "price",      invoice.getFinalAmount()
                                              .multiply(java.math.BigDecimal.valueOf(100))
                                              .intValue() // AbacatePay usa centavos
                    )}
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                    .uri("/billing/create")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                log.warn("[AbacatePay] Resposta nula para fatura #{}", invoice.getId());
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data == null) {
                log.warn("[AbacatePay] Campo 'data' ausente na resposta para fatura #{}", invoice.getId());
                return Optional.empty();
            }

            String externalId  = String.valueOf(data.getOrDefault("id", ""));
            String paymentLink = String.valueOf(data.getOrDefault("url", ""));

            // QR Code PIX pode vir aninhado em methods[0].pixQrCode dependendo da versão da API
            String pixQrCode = extractPixQrCode(data);

            log.info("[AbacatePay] Cobrança gerada com sucesso. externalId={} fatura=#{}",
                    externalId, invoice.getId());

            return Optional.of(new PixChargeResult(externalId, paymentLink, pixQrCode));

        } catch (Exception e) {
            // Fail-safe: gateway indisponível não deve impedir a geração da fatura
            log.error("[AbacatePay] Falha ao gerar cobrança PIX para fatura #{}: {}",
                    invoice.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractPixQrCode(Map<String, Object> data) {
        try {
            var methods = (java.util.List<Map<String, Object>>) data.get("methods");
            if (methods != null && !methods.isEmpty()) {
                return String.valueOf(methods.get(0).getOrDefault("pixQrCode", ""));
            }
        } catch (Exception ignored) {
            // estrutura inesperada — ignora silenciosamente
        }
        return null;
    }
}

