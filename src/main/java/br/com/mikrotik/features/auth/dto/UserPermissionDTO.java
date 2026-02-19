package br.com.mikrotik.features.auth.dto;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.auth.model.UserPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO para gerenciamento de permissões customizadas de usuário
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Permissão de acesso a módulo do sistema")
public class UserPermissionDTO {

    @Schema(description = "ID da permissão (somente leitura)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Módulo é obrigatório")
    @Schema(description = "Módulo do sistema", example = "CUSTOMERS", required = true)
    private SystemModule module;

    @NotEmpty(message = "Pelo menos uma ação deve ser selecionada")
    @Schema(description = "Ações permitidas no módulo",
            example = "[\"VIEW\", \"CREATE\", \"EDIT\"]",
            required = true)
    private Set<ModuleAction> actions;

    @Schema(description = "SubMódulos específicos (opcional)",
            example = "[\"FINANCIAL.CASH_FLOW\", \"FINANCIAL.TRANSACTIONS\"]")
    private String subModules;

    /**
     * Converte DTO para Entity
     */
    public UserPermission toEntity() {
        UserPermission permission = new UserPermission();
        permission.setId(this.id);
        permission.setModule(this.module);
        permission.setActions(this.actions);
        permission.setSubModules(this.subModules);
        return permission;
    }

    /**
     * Converte Entity para DTO
     */
    public static UserPermissionDTO fromEntity(UserPermission entity) {
        return UserPermissionDTO.builder()
                .id(entity.getId())
                .module(entity.getModule())
                .actions(entity.getActions())
                .subModules(entity.getSubModules())
                .build();
    }

    /**
     * Converte lista de entities para DTOs
     */
    public static Set<UserPermissionDTO> fromEntities(Set<UserPermission> entities) {
        return entities.stream()
                .map(UserPermissionDTO::fromEntity)
                .collect(Collectors.toSet());
    }
}

