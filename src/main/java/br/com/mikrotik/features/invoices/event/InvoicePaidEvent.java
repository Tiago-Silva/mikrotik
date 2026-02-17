package br.com.mikrotik.features.invoices.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evento publicado quando uma fatura é paga
 * Permite desacoplar a lógica de faturamento do fluxo de caixa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePaidEvent {

    private Long invoiceId;
    private Long companyId;
    private Long customerId;
    private Long contractId;
    private BigDecimal amountPaid;
    private LocalDateTime paidAt;
    private String paymentMethod;
    private String transactionCode;
}

