package br.com.mikrotik.features.contracts.dto;

import br.com.mikrotik.features.contracts.model.ServicePlan;
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
@Schema(description = "DTO para planos de serviço comerciais")
public class ServicePlanDTO {

    @Schema(description = "ID do plano", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @NotBlank(message = "Nome do plano é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    @Schema(description = "Nome do plano comercial", example = "Plano Gamer 500MB", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Descrição do plano", example = "Plano ideal para jogos online com baixa latência")
    private String description;

    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @Digits(integer = 17, fraction = 2, message = "Preço deve ter no máximo 2 casas decimais")
    @Schema(description = "Preço mensal do plano", example = "99.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "ID do perfil PPPoE é obrigatório")
    @Schema(description = "ID do perfil técnico PPPoE", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pppoeProfileId;

    @Schema(description = "Plano ativo", example = "true")
    private Boolean active;

    @Schema(description = "Nome do perfil PPPoE")
    private String pppoeProfileName;

    @Schema(description = "Velocidade de download (Mbps)")
    private String downloadSpeed;

    @Schema(description = "Velocidade de upload (Mbps)")
    private String uploadSpeed;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Converter de Entity para DTO
    public static ServicePlanDTO fromEntity(ServicePlan plan) {
        if (plan == null) {
            return null;
        }

        ServicePlanDTOBuilder builder = ServicePlanDTO.builder()
                .id(plan.getId())
                .companyId(plan.getCompanyId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .pppoeProfileId(plan.getPppoeProfileId())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt());

        // Adicionar informações do perfil PPPoE se disponível
        if (plan.getPppoeProfile() != null) {
            builder.pppoeProfileName(plan.getPppoeProfile().getName());

            // Converter bps para Mbps para exibição
            if (plan.getPppoeProfile().getMaxBitrateDl() != null) {
                builder.downloadSpeed(plan.getPppoeProfile().getMaxBitrateDl() / 1_000_000 + " Mbps");
            }
            if (plan.getPppoeProfile().getMaxBitrateUl() != null) {
                builder.uploadSpeed(plan.getPppoeProfile().getMaxBitrateUl() / 1_000_000 + " Mbps");
            }
        }

        return builder.build();
    }

    // Converter de DTO para Entity
    public ServicePlan toEntity() {
        return ServicePlan.builder()
                .id(this.id)
                .companyId(this.companyId)
                .name(this.name)
                .description(this.description)
                .price(this.price)
                .pppoeProfileId(this.pppoeProfileId)
                .active(this.active)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
