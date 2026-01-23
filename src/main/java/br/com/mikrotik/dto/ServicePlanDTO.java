package br.com.mikrotik.dto;

import br.com.mikrotik.model.ServicePlan;
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

    @NotNull(message = "ID do perfil de internet é obrigatório")
    @Schema(description = "ID do perfil técnico de internet", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long internetProfileId;

    @Schema(description = "Plano ativo", example = "true")
    private Boolean active;

    @Schema(description = "Nome do perfil de internet")
    private String internetProfileName;

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
                .internetProfileId(plan.getInternetProfileId())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt());

        // Adicionar informações do internet profile se disponível
        if (plan.getInternetProfile() != null) {
            builder.internetProfileName(plan.getInternetProfile().getName());

            // Converter kbits para Mbps para exibição
            if (plan.getInternetProfile().getDownloadKbit() != null) {
                builder.downloadSpeed(plan.getInternetProfile().getDownloadKbit() / 1024 + " Mbps");
            }
            if (plan.getInternetProfile().getUploadKbit() != null) {
                builder.uploadSpeed(plan.getInternetProfile().getUploadKbit() / 1024 + " Mbps");
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
                .internetProfileId(this.internetProfileId)
                .active(this.active)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
