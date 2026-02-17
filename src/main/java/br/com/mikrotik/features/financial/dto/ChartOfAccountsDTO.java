package br.com.mikrotik.features.financial.dto;

import br.com.mikrotik.features.financial.model.ChartOfAccounts;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para plano de contas (DRE)")
public class ChartOfAccountsDTO {

    @Schema(description = "ID da conta", example = "1")
    private Long id;

    @Schema(description = "ID da empresa")
    private Long companyId;

    @NotBlank(message = "Código da conta é obrigatório")
    @Schema(description = "Código da conta", example = "1.1.01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "Nome da conta é obrigatório")
    @Schema(description = "Nome da conta", example = "Receita de Serviços - Internet", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Tipo de conta é obrigatório")
    @Schema(description = "Tipo de conta", example = "REVENUE",
            allowableValues = {"REVENUE", "EXPENSE", "ASSET", "LIABILITY", "EQUITY"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private ChartOfAccounts.AccountType accountType;

    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "Categoria da conta", example = "SUBSCRIPTION_REVENUE", requiredMode = Schema.RequiredMode.REQUIRED)
    private ChartOfAccounts.Category category;

    @Schema(description = "ID da conta pai (para hierarquia)", example = "1")
    private Long parentId;

    @Schema(description = "Conta ativa?", example = "true")
    private Boolean active;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    private LocalDateTime updatedAt;

    public static ChartOfAccountsDTO fromEntity(ChartOfAccounts entity) {
        if (entity == null) {
            return null;
        }

        return ChartOfAccountsDTO.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .code(entity.getCode())
                .name(entity.getName())
                .accountType(entity.getAccountType())
                .category(entity.getCategory())
                .parentId(entity.getParentId())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ChartOfAccounts toEntity() {
        return ChartOfAccounts.builder()
                .id(this.id)
                .companyId(this.companyId)
                .code(this.code)
                .name(this.name)
                .accountType(this.accountType)
                .category(this.category)
                .parentId(this.parentId)
                .active(this.active)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

