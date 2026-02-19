package br.com.mikrotik.features.auth.model;

import lombok.Getter;

/**
 * Ações disponíveis que podem ser executadas em cada módulo
 * Controle granular de operações (CRUD + Execução)
 */
@Getter
public enum ModuleAction {

    VIEW("Visualizar", "Permite visualizar dados do módulo"),
    CREATE("Criar", "Permite criar novos registros"),
    EDIT("Editar", "Permite editar registros existentes"),
    DELETE("Excluir", "Permite excluir registros"),
    EXECUTE("Executar", "Permite executar ações especiais (jobs, sincronização)");

    private final String displayName;
    private final String description;

    ModuleAction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Retorna ações padrão para role VIEWER
     */
    public static ModuleAction[] getViewerActions() {
        return new ModuleAction[]{VIEW};
    }

    /**
     * Retorna ações padrão para roles operacionais
     */
    public static ModuleAction[] getOperatorActions() {
        return new ModuleAction[]{VIEW, CREATE, EDIT};
    }

    /**
     * Retorna ações padrão para ADMIN (todas)
     */
    public static ModuleAction[] getAdminActions() {
        return values();
    }

    /**
     * Verifica se é uma ação de escrita
     */
    public boolean isWriteAction() {
        return this == CREATE || this == EDIT || this == DELETE;
    }

    /**
     * Verifica se é uma ação perigosa (requer confirmação)
     */
    public boolean isDangerous() {
        return this == DELETE || this == EXECUTE;
    }
}

