package br.com.mikrotik.features.auth.dto;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * DTO para listar módulos disponíveis no sistema
 * Usado em formulários de seleção de permissões
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Módulos disponíveis no sistema")
public class AvailableModulesDTO {

    @Schema(description = "Lista de todos os módulos do sistema")
    private List<ModuleInfo> modules;

    @Schema(description = "Lista de todas as ações disponíveis")
    private List<ActionInfo> actions;

    /**
     * Informações de um módulo
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Informações de um módulo do sistema")
    public static class ModuleInfo {

        @Schema(description = "Nome interno do módulo", example = "CUSTOMERS")
        private String name;

        @Schema(description = "Nome de exibição", example = "Clientes")
        private String displayName;

        @Schema(description = "Descrição", example = "Cadastro e gestão de clientes")
        private String description;

        @Schema(description = "Prefixo da rota", example = "customers")
        private String path;

        @Schema(description = "Se é um módulo administrativo", example = "false")
        private boolean adminOnly;

        @Schema(description = "Se é um módulo crítico", example = "true")
        private boolean critical;

        public static ModuleInfo fromEnum(SystemModule module) {
            return ModuleInfo.builder()
                    .name(module.name())
                    .displayName(module.getDisplayName())
                    .description(module.getDescription())
                    .path(module.getPath())
                    .adminOnly(module.isAdminOnly())
                    .critical(module.isCritical())
                    .build();
        }
    }

    /**
     * Informações de uma ação
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Informações de uma ação disponível")
    public static class ActionInfo {

        @Schema(description = "Nome interno da ação", example = "VIEW")
        private String name;

        @Schema(description = "Nome de exibição", example = "Visualizar")
        private String displayName;

        @Schema(description = "Descrição", example = "Permite visualizar dados do módulo")
        private String description;

        @Schema(description = "Se é uma ação de escrita", example = "false")
        private boolean writeAction;

        @Schema(description = "Se é uma ação perigosa", example = "false")
        private boolean dangerous;

        public static ActionInfo fromEnum(ModuleAction action) {
            return ActionInfo.builder()
                    .name(action.name())
                    .displayName(action.getDisplayName())
                    .description(action.getDescription())
                    .writeAction(action.isWriteAction())
                    .dangerous(action.isDangerous())
                    .build();
        }
    }

    /**
     * Cria DTO com todos os módulos e ações disponíveis
     */
    public static AvailableModulesDTO createFull() {
        List<ModuleInfo> modules = Arrays.stream(SystemModule.values())
                .map(ModuleInfo::fromEnum)
                .toList();

        List<ActionInfo> actions = Arrays.stream(ModuleAction.values())
                .map(ActionInfo::fromEnum)
                .toList();

        return AvailableModulesDTO.builder()
                .modules(modules)
                .actions(actions)
                .build();
    }
}

