package br.com.mikrotik.shared.infrastructure.payment;

/**
 * Resultado imutável de uma cobrança PIX gerada pelo gateway de pagamento.
 *
 * @param externalId  ID gerado pelo gateway (ex: "chr_abc123") — usado para reconciliação
 * @param paymentLink URL ou copia-e-cola da cobrança para o cliente acessar/pagar
 * @param pixQrCode   Payload do QR Code PIX (EMV), se disponível
 */
public record PixChargeResult(
        String externalId,
        String paymentLink,
        String pixQrCode
) {}

