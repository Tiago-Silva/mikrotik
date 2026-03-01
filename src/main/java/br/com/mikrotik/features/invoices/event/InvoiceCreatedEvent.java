package br.com.mikrotik.features.invoices.event;

/**
 * Evento publicado após uma fatura ser persistida com sucesso (status PENDING).
 *
 * Consumido pelo AbacatePayEventListener para gerar o link/QR Code PIX
 * de forma assíncrona e FORA da transação original (AFTER_COMMIT).
 *
 * @param invoiceId  ID da fatura já salva no banco
 * @param companyId  ID da empresa (multi-tenant context)
 */
public record InvoiceCreatedEvent(
        Long invoiceId,
        Long companyId
) {}

