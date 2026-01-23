package br.com.mikrotik.dto;

import br.com.mikrotik.model.Transaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para transações de pagamento")
public class TransactionDTO {

    @Schema(description = "ID da transação", example = "1")
    private Long id;

    @NotNull(message = "ID da fatura é obrigatório")
    @Schema(description = "ID da fatura", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long invoiceId;

    @NotNull(message = "Valor pago é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Valor pago", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amountPaid;

    @NotNull(message = "Data de pagamento é obrigatória")
    @Schema(description = "Data e hora do pagamento", example = "2026-01-22T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paidAt;

    @NotNull(message = "Método de pagamento é obrigatório")
    @Schema(description = "Método de pagamento", example = "PIX",
            allowableValues = {"BOLETO", "PIX", "CREDIT_CARD", "CASH", "TRANSFER"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Transaction.PaymentMethod method;

    @Schema(description = "Código da transação", example = "TXN-2026-001")
    private String transactionCode;

    @Schema(description = "Observações sobre o pagamento", example = "Pagamento via PIX")
    private String notes;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Campos informativos
    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "Descrição da fatura")
    private String invoiceDescription;

    @Schema(description = "Valor original da fatura")
    private BigDecimal invoiceAmount;

    // Converter de Entity para DTO
    public static TransactionDTO fromEntity(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDTOBuilder builder = TransactionDTO.builder()
                .id(transaction.getId())
                .invoiceId(transaction.getInvoiceId())
                .amountPaid(transaction.getAmountPaid())
                .paidAt(transaction.getPaidAt())
                .method(transaction.getMethod())
                .transactionCode(transaction.getTransactionCode())
                .notes(transaction.getNotes())
                .createdAt(transaction.getCreatedAt());

        // Informações da fatura
        if (transaction.getInvoice() != null) {
            builder.invoiceDescription(transaction.getInvoice().getDescription());
            builder.invoiceAmount(transaction.getInvoice().getFinalAmount());

            if (transaction.getInvoice().getCustomer() != null) {
                builder.customerName(transaction.getInvoice().getCustomer().getName());
            }
        }

        return builder.build();
    }

    // Converter de DTO para Entity
    public Transaction toEntity() {
        return Transaction.builder()
                .id(this.id)
                .invoiceId(this.invoiceId)
                .amountPaid(this.amountPaid)
                .paidAt(this.paidAt)
                .method(this.method)
                .transactionCode(this.transactionCode)
                .notes(this.notes)
                .createdAt(this.createdAt)
                .build();
    }
}
