package br.com.mikrotik.features.invoices.dto;

import br.com.mikrotik.features.invoices.model.Invoice;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para faturas")
public class InvoiceDTO {

    @Schema(description = "ID da fatura", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @NotNull(message = "ID do contrato é obrigatório")
    @Schema(description = "ID do contrato", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long contractId;

    @NotNull(message = "ID do cliente é obrigatório")
    @Schema(description = "ID do cliente", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @Schema(description = "Descrição da fatura", example = "Mensalidade Internet - Janeiro/2026")
    private String description;

    @NotNull(message = "Mês de referência é obrigatório")
    @Schema(description = "Mês de referência", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate referenceMonth;

    @NotNull(message = "Data de vencimento é obrigatória")
    @Schema(description = "Data de vencimento", example = "2026-01-10", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @NotNull(message = "Valor original é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Valor original", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal originalAmount;

    @DecimalMin(value = "0.00")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Valor de desconto", example = "0.00")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Valor de juros", example = "0.00")
    private BigDecimal interestAmount;

    @NotNull(message = "Valor final é obrigatório")
    @DecimalMin(value = "0.01")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Valor final a pagar", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal finalAmount;

    @Schema(description = "Status da fatura", example = "PENDING",
            allowableValues = {"PENDING", "PAID", "PARTIALLY_PAID", "OVERDUE", "CANCELED", "REFUNDED"})
    private Invoice.InvoiceStatus status;

    @Schema(description = "Link de pagamento", example = "https://pagamento.com/fatura/123")
    private String paymentLink;

    @Schema(description = "ID externo (gateway de pagamento)", example = "INV-2026-001")
    private String externalId;

    // Campos informativos
    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "Documento do cliente")
    private String customerDocument;

    @Schema(description = "Nome do plano de serviço")
    private String servicePlanName;

    @Schema(description = "Dias em atraso")
    private Long daysOverdue;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Converter de Entity para DTO
    public static InvoiceDTO fromEntity(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        InvoiceDTOBuilder builder = InvoiceDTO.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .contractId(invoice.getContractId())
                .customerId(invoice.getCustomerId())
                .description(invoice.getDescription())
                .referenceMonth(invoice.getReferenceMonth())
                .dueDate(invoice.getDueDate())
                .originalAmount(invoice.getOriginalAmount())
                .discountAmount(invoice.getDiscountAmount())
                .interestAmount(invoice.getInterestAmount())
                .finalAmount(invoice.getFinalAmount())
                .status(invoice.getStatus())
                .paymentLink(invoice.getPaymentLink())
                .externalId(invoice.getExternalId())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt());

        // Informações do cliente
        if (invoice.getCustomer() != null) {
            builder.customerName(invoice.getCustomer().getName());
            builder.customerDocument(invoice.getCustomer().getDocument());
        }

        // Informações do plano
        if (invoice.getContract() != null && invoice.getContract().getServicePlan() != null) {
            builder.servicePlanName(invoice.getContract().getServicePlan().getName());
        }

        // Calcular dias de atraso
        if (invoice.getStatus() == Invoice.InvoiceStatus.OVERDUE ||
            invoice.getStatus() == Invoice.InvoiceStatus.PENDING) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(invoice.getDueDate())) {
                builder.daysOverdue(today.toEpochDay() - invoice.getDueDate().toEpochDay());
            }
        }

        return builder.build();
    }

    // Converter de DTO para Entity
    public Invoice toEntity() {
        return Invoice.builder()
                .id(this.id)
                .companyId(this.companyId)
                .contractId(this.contractId)
                .customerId(this.customerId)
                .description(this.description)
                .referenceMonth(this.referenceMonth)
                .dueDate(this.dueDate)
                .originalAmount(this.originalAmount)
                .discountAmount(this.discountAmount)
                .interestAmount(this.interestAmount)
                .finalAmount(this.finalAmount)
                .status(this.status)
                .paymentLink(this.paymentLink)
                .externalId(this.externalId)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
