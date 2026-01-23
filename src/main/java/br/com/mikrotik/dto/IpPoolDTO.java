package br.com.mikrotik.dto;

import br.com.mikrotik.model.IpPool;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para pools de IP")
public class IpPoolDTO {

    @Schema(description = "ID do pool", example = "1")
    private Long id;

    @NotNull(message = "ID do servidor Mikrotik é obrigatório")
    @Schema(description = "ID do servidor Mikrotik", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long mikrotikServerId;

    @NotBlank(message = "Nome do pool é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome do pool", example = "pool_cgnat_01", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 20, message = "CIDR deve ter no máximo 20 caracteres")
    @Schema(description = "Range de IPs em formato CIDR", example = "100.64.0.0/24")
    private String cidr;

    @Schema(description = "Pool ativo", example = "true")
    private Boolean active;

    @Schema(description = "Nome do servidor Mikrotik")
    private String mikrotikServerName;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Converter de Entity para DTO
    public static IpPoolDTO fromEntity(IpPool ipPool) {
        if (ipPool == null) {
            return null;
        }

        return IpPoolDTO.builder()
                .id(ipPool.getId())
                .mikrotikServerId(ipPool.getMikrotikServerId())
                .name(ipPool.getName())
                .cidr(ipPool.getCidr())
                .active(ipPool.getActive())
                .mikrotikServerName(ipPool.getMikrotikServer() != null ? ipPool.getMikrotikServer().getName() : null)
                .createdAt(ipPool.getCreatedAt())
                .build();
    }

    // Converter de DTO para Entity
    public IpPool toEntity() {
        return IpPool.builder()
                .id(this.id)
                .mikrotikServerId(this.mikrotikServerId)
                .name(this.name)
                .cidr(this.cidr)
                .active(this.active)
                .createdAt(this.createdAt)
                .build();
    }
}
