package br.com.mikrotik.dto;

import br.com.mikrotik.model.Contract;
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
@Schema(description = "DTO para contratos de serviço")
public class ContractDTO {

    @Schema(description = "ID do contrato", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @NotNull(message = "ID do cliente é obrigatório")
    @Schema(description = "ID do cliente", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @NotNull(message = "ID do plano de serviço é obrigatório")
    @Schema(description = "ID do plano de serviço", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long servicePlanId;

    @Schema(description = "ID do usuário PPPoE", example = "1")
    private Long pppoeUserId;

    @Schema(description = "ID do endereço de instalação", example = "1")
    private Long installationAddressId;

    @Schema(description = "Status do contrato", example = "ACTIVE",
            allowableValues = {"DRAFT", "ACTIVE", "SUSPENDED_FINANCIAL", "SUSPENDED_REQUEST", "CANCELED"})
    private Contract.ContractStatus status;

    @NotNull(message = "Dia de cobrança é obrigatório")
    @Min(value = 1, message = "Dia de cobrança deve ser entre 1 e 31")
    @Max(value = 31, message = "Dia de cobrança deve ser entre 1 e 31")
    @Schema(description = "Dia do mês para cobrança", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer billingDay;

    @NotNull(message = "Valor do contrato é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 17, fraction = 2, message = "Valor deve ter no máximo 2 casas decimais")
    @Schema(description = "Valor mensal do contrato", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "Data de início é obrigatória")
    @Schema(description = "Data de início do contrato", example = "2026-01-22", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "Data de término do contrato (null = indeterminado)", example = "2027-01-22")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "Data de cancelamento", example = "2026-12-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate cancellationDate;

    // Campos informativos (não editáveis)
    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "Documento do cliente")
    private String customerDocument;

    @Schema(description = "Nome do plano de serviço")
    private String servicePlanName;

    @Schema(description = "Username do usuário PPPoE")
    private String pppoeUsername;

    @Schema(description = "Endereço de instalação completo")
    private String installationAddress;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Converter de Entity para DTO
    public static ContractDTO fromEntity(Contract contract) {
        if (contract == null) {
            return null;
        }

        ContractDTOBuilder builder = ContractDTO.builder()
                .id(contract.getId())
                .companyId(contract.getCompanyId())
                .customerId(contract.getCustomerId())
                .servicePlanId(contract.getServicePlanId())
                .pppoeUserId(contract.getPppoeUserId())
                .installationAddressId(contract.getInstallationAddressId())
                .status(contract.getStatus())
                .billingDay(contract.getBillingDay())
                .amount(contract.getAmount())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .cancellationDate(contract.getCancellationDate())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt());

        // Adicionar informações relacionadas se disponíveis
        if (contract.getCustomer() != null) {
            builder.customerName(contract.getCustomer().getName());
            builder.customerDocument(contract.getCustomer().getDocument());
        }

        if (contract.getServicePlan() != null) {
            builder.servicePlanName(contract.getServicePlan().getName());
        }

        if (contract.getPppoeUser() != null) {
            builder.pppoeUsername(contract.getPppoeUser().getUsername());
        }

        if (contract.getInstallationAddress() != null) {
            String address = String.format("%s, %s - %s, %s/%s",
                    contract.getInstallationAddress().getStreet(),
                    contract.getInstallationAddress().getNumber(),
                    contract.getInstallationAddress().getDistrict(),
                    contract.getInstallationAddress().getCity(),
                    contract.getInstallationAddress().getState());
            builder.installationAddress(address);
        }

        return builder.build();
    }

    // Converter de DTO para Entity
    public Contract toEntity() {
        return Contract.builder()
                .id(this.id)
                .companyId(this.companyId)
                .customerId(this.customerId)
                .servicePlanId(this.servicePlanId)
                .pppoeUserId(this.pppoeUserId)
                .installationAddressId(this.installationAddressId)
                .status(this.status)
                .billingDay(this.billingDay)
                .amount(this.amount)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .cancellationDate(this.cancellationDate)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
