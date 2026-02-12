package br.com.mikrotik.model;
import lombok.Getter;
/**
 * Roles de usuários do sistema com controle hierárquico de permissões
 */
@Getter
public enum UserRole {
    ADMIN("Administrador", "Acesso completo ao sistema", 100),
    OPERATOR("Operador", "Acesso para operações do dia a dia", 75),
    FINANCIAL("Financeiro", "Acesso ao módulo financeiro", 50),
    TECHNICAL("Técnico", "Acesso ao módulo técnico/Mikrotik", 50),
    VIEWER("Visualizador", "Apenas visualização de dados", 25);
    private final String displayName;
    private final String description;
    private final int level; // Nível de permissão (maior = mais poder)
    UserRole(String displayName, String description, int level) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
    }
    /**
     * Verifica se a role tem permissões administrativas
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    /**
     * Verifica se a role pode realizar operações
     */
    public boolean canOperate() {
        return this == ADMIN || this == OPERATOR || this == TECHNICAL;
    }
    /**
     * Verifica se a role tem acesso financeiro
     */
    public boolean hasFinancialAccess() {
        return this == ADMIN || this == FINANCIAL;
    }
    /**
     * Verifica se a role tem acesso técnico
     */
    public boolean hasTechnicalAccess() {
        return this == ADMIN || this == TECHNICAL || this == OPERATOR;
    }
    /**
     * Verifica se esta role tem permissão maior ou igual a outra
     */
    public boolean hasPermissionLevel(UserRole other) {
        return this.level >= other.level;
    }
}
