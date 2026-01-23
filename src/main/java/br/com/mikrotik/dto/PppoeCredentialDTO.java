package br.com.mikrotik.dto;

import br.com.mikrotik.model.PppoeCredential;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para credenciais PPPoE")
public class PppoeCredentialDTO {

    @Schema(description = "ID da credencial", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @NotNull(message = "ID do servidor Mikrotik é obrigatório")
    @Schema(description = "ID do servidor Mikrotik", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long mikrotikServerId;

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 100, message = "Username deve ter entre 3 e 100 caracteres")
    @Schema(description = "Username PPPoE", example = "cliente001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password é obrigatório")
    @Size(min = 6, max = 100, message = "Password deve ter entre 6 e 100 caracteres")
    @Schema(description = "Password PPPoE", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "MAC address inválido")
    @Schema(description = "MAC address do cliente", example = "00:11:22:33:44:55")
    private String macAddress;

    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "IP inválido")
    @Schema(description = "IP estático (opcional)", example = "10.10.10.100")
    private String staticIp;

    @Schema(description = "Status da credencial", example = "OFFLINE", allowableValues = {"ONLINE", "OFFLINE", "DISABLED"})
    private PppoeCredential.CredentialStatus status;

    @Schema(description = "Nome do servidor Mikrotik")
    private String mikrotikServerName;

    @Schema(description = "Data da última conexão")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastConnectionAt;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Converter de Entity para DTO
    public static PppoeCredentialDTO fromEntity(PppoeCredential credential) {
        if (credential == null) {
            return null;
        }

        return PppoeCredentialDTO.builder()
                .id(credential.getId())
                .companyId(credential.getCompanyId())
                .mikrotikServerId(credential.getMikrotikServerId())
                .username(credential.getUsername())
                .password(credential.getPassword())
                .macAddress(credential.getMacAddress())
                .staticIp(credential.getStaticIp())
                .status(credential.getStatus())
                .mikrotikServerName(credential.getMikrotikServer() != null ? credential.getMikrotikServer().getName() : null)
                .lastConnectionAt(credential.getLastConnectionAt())
                .createdAt(credential.getCreatedAt())
                .updatedAt(credential.getUpdatedAt())
                .build();
    }

    // Converter de DTO para Entity
    public PppoeCredential toEntity() {
        return PppoeCredential.builder()
                .id(this.id)
                .companyId(this.companyId)
                .mikrotikServerId(this.mikrotikServerId)
                .username(this.username)
                .password(this.password)
                .macAddress(this.macAddress)
                .staticIp(this.staticIp)
                .status(this.status)
                .lastConnectionAt(this.lastConnectionAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
