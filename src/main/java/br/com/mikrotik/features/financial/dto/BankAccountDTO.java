package br.com.mikrotik.features.financial.dto;

import br.com.mikrotik.features.financial.model.BankAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para contas bancárias")
public class BankAccountDTO {

    @Schema(description = "ID da conta", example = "1")
    private Long id;

    @Schema(description = "ID da empresa")
    private Long companyId;

    @NotBlank(message = "Nome da conta é obrigatório")
    @Schema(description = "Nome da conta", example = "Banco do Brasil - CC 12345-6", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Tipo de conta é obrigatório")
    @Schema(description = "Tipo de conta", example = "CHECKING",
            allowableValues = {"CHECKING", "SAVINGS", "CASH", "CASH_INTERNAL", "DIGITAL_WALLET", "CREDIT_CARD"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private BankAccount.AccountType accountType;

    @Schema(description = "Código do banco", example = "001")
    private String bankCode;

    @Schema(description = "Agência", example = "1234")
    private String agency;

    @Schema(description = "Número da conta", example = "12345-6")
    private String accountNumber;

    @DecimalMin(value = "0.0", message = "Saldo inicial não pode ser negativo")
    @Digits(integer = 17, fraction = 2)
    @Schema(description = "Saldo inicial", example = "1000.00")
    private BigDecimal initialBalance;

    @Schema(description = "Saldo atual", example = "1500.00")
    private BigDecimal currentBalance;

    @Schema(description = "Conta ativa?", example = "true")
    private Boolean active;

    @Schema(description = "Observações")
    private String notes;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    private LocalDateTime updatedAt;

    public static BankAccountDTO fromEntity(BankAccount entity) {
        if (entity == null) {
            return null;
        }

        return BankAccountDTO.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .name(entity.getName())
                .accountType(entity.getAccountType())
                .bankCode(entity.getBankCode())
                .agency(entity.getAgency())
                .accountNumber(entity.getAccountNumber())
                .initialBalance(entity.getInitialBalance())
                .currentBalance(entity.getCurrentBalance())
                .active(entity.getActive())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public BankAccount toEntity() {
        return BankAccount.builder()
                .id(this.id)
                .companyId(this.companyId)
                .name(this.name)
                .accountType(this.accountType)
                .bankCode(this.bankCode)
                .agency(this.agency)
                .accountNumber(this.accountNumber)
                .initialBalance(this.initialBalance != null ? this.initialBalance : BigDecimal.ZERO)
                .currentBalance(this.currentBalance != null ? this.currentBalance :
                               (this.initialBalance != null ? this.initialBalance : BigDecimal.ZERO))
                .active(this.active != null ? this.active : true)
                .notes(this.notes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

