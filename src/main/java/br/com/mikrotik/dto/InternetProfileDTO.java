package br.com.mikrotik.dto;

import br.com.mikrotik.model.InternetProfile;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para perfis técnicos de internet")
public class InternetProfileDTO {

    @Schema(description = "ID do perfil", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @NotNull(message = "ID do servidor Mikrotik é obrigatório")
    @Schema(description = "ID do servidor Mikrotik", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long mikrotikServerId;

    @NotBlank(message = "Nome do perfil é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    @Schema(description = "Nome do perfil", example = "500MB_FIBRA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Descrição do perfil", example = "Perfil 500 Mega Fibra Óptica")
    private String description;

    @NotNull(message = "Velocidade de download é obrigatória")
    @Min(value = 1, message = "Velocidade de download deve ser maior que 0")
    @Schema(description = "Velocidade de download em kbit/s", example = "512000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long downloadKbit;

    @NotNull(message = "Velocidade de upload é obrigatória")
    @Min(value = 1, message = "Velocidade de upload deve ser maior que 0")
    @Schema(description = "Velocidade de upload em kbit/s", example = "256000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uploadKbit;

    @Schema(description = "Timeout da sessão em segundos (0 = sem limite)", example = "0")
    private Integer sessionTimeout;

    @Schema(description = "ID do pool de IPs remoto", example = "1")
    private Long remoteAddressPoolId;

    @Schema(description = "Perfil ativo", example = "true")
    private Boolean active;

    @Schema(description = "Nome do servidor Mikrotik")
    private String mikrotikServerName;

    @Schema(description = "Nome do pool de IPs")
    private String poolName;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Converter de Entity para DTO
    public static InternetProfileDTO fromEntity(InternetProfile profile) {
        if (profile == null) {
            return null;
        }

        return InternetProfileDTO.builder()
                .id(profile.getId())
                .companyId(profile.getCompanyId())
                .mikrotikServerId(profile.getMikrotikServerId())
                .name(profile.getName())
                .description(profile.getDescription())
                .downloadKbit(profile.getDownloadKbit())
                .uploadKbit(profile.getUploadKbit())
                .sessionTimeout(profile.getSessionTimeout())
                .remoteAddressPoolId(profile.getRemoteAddressPoolId())
                .active(profile.getActive())
                .mikrotikServerName(profile.getMikrotikServer() != null ? profile.getMikrotikServer().getName() : null)
                .poolName(profile.getRemoteAddressPool() != null ? profile.getRemoteAddressPool().getName() : null)
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    // Converter de DTO para Entity
    public InternetProfile toEntity() {
        return InternetProfile.builder()
                .id(this.id)
                .companyId(this.companyId)
                .mikrotikServerId(this.mikrotikServerId)
                .name(this.name)
                .description(this.description)
                .downloadKbit(this.downloadKbit)
                .uploadKbit(this.uploadKbit)
                .sessionTimeout(this.sessionTimeout)
                .remoteAddressPoolId(this.remoteAddressPoolId)
                .active(this.active)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
