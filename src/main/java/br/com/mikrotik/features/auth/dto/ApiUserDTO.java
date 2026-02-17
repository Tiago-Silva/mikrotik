package br.com.mikrotik.features.auth.dto;
import br.com.mikrotik.features.auth.model.ApiUser;
import br.com.mikrotik.features.auth.model.UserRole;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para gerenciamento de usuários da API")
public class ApiUserDTO {
    @Schema(description = "ID do usuário", example = "1")
    private Long id;
    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 255, message = "Username deve ter entre 3 e 255 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username deve conter apenas letras, números, ponto, hífen e underscore")
    @Schema(description = "Nome de usuário", example = "joao.silva", required = true)
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Senha do usuário (mínimo 6 caracteres)", example = "senha123", required = true)
    private String password;
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    @Schema(description = "Email do usuário", example = "joao@example.com", required = true)
    private String email;
    @NotNull(message = "Role é obrigatória")
    @Schema(description = "Permissão do usuário", example = "OPERATOR", required = true, 
            allowableValues = {"ADMIN", "OPERATOR", "FINANCIAL", "TECHNICAL", "VIEWER"})
    private UserRole role;
    @Schema(description = "Usuário ativo", example = "true")
    @Builder.Default
    private Boolean active = true;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2026-01-01T10:00:00")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de última atualização", example = "2026-01-01T10:00:00")
    private LocalDateTime updatedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data do último login", example = "2026-01-01T10:00:00")
    private LocalDateTime lastLogin;
    // Campos auxiliares para exibição
    @Schema(description = "Nome de exibição da role", example = "Operador")
    private String roleDisplayName;
    @Schema(description = "Descrição da role", example = "Acesso para operações do dia a dia")
    private String roleDescription;
    /**
     * Converte DTO para Entity
     */
    public ApiUser toEntity() {
        return ApiUser.builder()
                .id(this.id)
                .companyId(this.companyId)
                .username(this.username)
                .password(this.password) // Será criptografada no service
                .email(this.email)
                .role(this.role != null ? this.role : UserRole.VIEWER)
                .active(this.active != null ? this.active : true)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .lastLogin(this.lastLogin)
                .build();
    }
    /**
     * Converte Entity para DTO
     */
    public static ApiUserDTO fromEntity(ApiUser entity) {
        if (entity == null) {
            return null;
        }
        UserRole role = entity.getRole() != null ? entity.getRole() : UserRole.VIEWER;
        return ApiUserDTO.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .username(entity.getUsername())
                // password não é retornado
                .email(entity.getEmail())
                .role(role)
                .roleDisplayName(role.getDisplayName())
                .roleDescription(role.getDescription())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastLogin(entity.getLastLogin())
                .build();
    }
    /**
     * DTO para troca de senha
     */
    @Data
    @Schema(description = "DTO para alteração de senha")
    public static class ChangePasswordDTO {
        @NotBlank(message = "Senha atual é obrigatória")
        @Schema(description = "Senha atual", example = "senhaAntiga123", required = true)
        private String currentPassword;
        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 6, message = "Nova senha deve ter no mínimo 6 caracteres")
        @Schema(description = "Nova senha (mínimo 6 caracteres)", example = "novaSenha123", required = true)
        private String newPassword;
        @NotBlank(message = "Confirmação de senha é obrigatória")
        @Schema(description = "Confirmação da nova senha", example = "novaSenha123", required = true)
        private String confirmPassword;
    }
    /**
     * DTO para reset de senha (admin)
     */
    @Data
    @Schema(description = "DTO para reset de senha pelo admin")
    public static class ResetPasswordDTO {
        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 6, message = "Nova senha deve ter no mínimo 6 caracteres")
        @Schema(description = "Nova senha (mínimo 6 caracteres)", example = "novaSenha123", required = true)
        private String newPassword;
    }
}
