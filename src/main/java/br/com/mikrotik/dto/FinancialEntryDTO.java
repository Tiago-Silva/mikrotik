package br.com.mikrotik.dto;

import br.com.mikrotik.model.FinancialEntry;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para lançamentos financeiros")
public class FinancialEntryDTO {

    @Schema(description = "ID do lançamento", example = "1")
    private Long id;

    @Schema(description = "ID da empresa")
    private Long companyId;

    @NotNull(message = "ID da conta bancária é obrigatório")
    @Schema(description = "ID da conta bancária", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bankAccountId;

    @NotNull(message = "ID da conta do plano de contas é obrigatório")
    @Schema(description = "ID da conta do plano de contas", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chartOfAccountId;

    @NotNull(message = "Tipo de entrada é obrigatório")
    @Schema(description = "Tipo de entrada", example = "CREDIT",
            allowableValues = {"CREDIT", "DEBIT", "REVERSAL"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private FinancialEntry.EntryType entryType;

    @NotNull(message = "Tipo de transação é obrigatório")
    @Schema(description = "Tipo de transação", example = "INVOICE_PAYMENT",
            allowableValues = {"INVOICE_PAYMENT", "MANUAL_ENTRY", "TRANSFER", "ADJUSTMENT", "REFUND"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private FinancialEntry.TransactionType transactionType;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Valor", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "Descrição é obrigatória")
    @Schema(description = "Descrição do lançamento", example = "Pagamento fatura #123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "Data de competência é obrigatória")
    @Schema(description = "Data de competência", example = "2026-02-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate referenceDate;

    @NotNull(message = "Data efetiva é obrigatória")
    @Schema(description = "Data efetiva", example = "2026-02-01T14:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveDate;

    @Schema(description = "ID da fatura (se aplicável)", example = "1")
    private Long invoiceId;

    @Schema(description = "ID do lançamento original (para estornos)", example = "10")
    private Long reversedFromId;

    @Schema(description = "Status do lançamento", example = "ACTIVE",
            allowableValues = {"ACTIVE", "REVERSED", "CANCELLED"})
    private FinancialEntry.Status status;

    @Schema(description = "Observações")
    private String notes;

    @Schema(description = "ID do usuário que criou")
    private Long createdBy;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Campos informativos
    @Schema(description = "Nome da conta bancária")
    private String bankAccountName;

    @Schema(description = "Nome da conta do plano de contas")
    private String chartOfAccountName;

    public static FinancialEntryDTO fromEntity(FinancialEntry entity) {
        if (entity == null) {
            return null;
        }

        FinancialEntryDTOBuilder builder = FinancialEntryDTO.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .bankAccountId(entity.getBankAccountId())
                .chartOfAccountId(entity.getChartOfAccountId())
                .entryType(entity.getEntryType())
                .transactionType(entity.getTransactionType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .referenceDate(entity.getReferenceDate())
                .effectiveDate(entity.getEffectiveDate())
                .invoiceId(entity.getInvoiceId())
                .reversedFromId(entity.getReversedFromId())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // Informações relacionadas
        if (entity.getBankAccount() != null) {
            builder.bankAccountName(entity.getBankAccount().getName());
        }
        if (entity.getChartOfAccount() != null) {
            builder.chartOfAccountName(entity.getChartOfAccount().getName());
        }

        return builder.build();
    }

    public FinancialEntry toEntity() {
        return FinancialEntry.builder()
                .id(this.id)
                .companyId(this.companyId)
                .bankAccountId(this.bankAccountId)
                .chartOfAccountId(this.chartOfAccountId)
                .entryType(this.entryType)
                .transactionType(this.transactionType)
                .amount(this.amount)
                .description(this.description)
                .referenceDate(this.referenceDate)
                .effectiveDate(this.effectiveDate)
                .invoiceId(this.invoiceId)
                .reversedFromId(this.reversedFromId)
                .status(this.status)
                .notes(this.notes)
                .createdBy(this.createdBy)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

