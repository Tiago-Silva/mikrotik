package br.com.mikrotik.features.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Permissões customizadas por usuário
 * Permite controle granular de acesso a módulos e ações específicas
 */
@Entity
@Table(name = "user_permissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "module"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private ApiUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SystemModule module;

    /**
     * Ações permitidas neste módulo (VIEW, CREATE, EDIT, DELETE, EXECUTE)
     * Armazenado como String separado por vírgula: "VIEW,CREATE,EDIT"
     */
    @Column(name = "actions", nullable = false, length = 255)
    private String actionsString;

    /**
     * SubMódulos específicos (opcional - JSON ou String)
     * Ex: "FINANCIAL.CASH_FLOW,FINANCIAL.TRANSACTIONS"
     * Null = acesso a todo o módulo
     */
    @Column(name = "sub_modules", columnDefinition = "TEXT")
    private String subModules;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Atualiza o timestamp de última modificação
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== MÉTODOS DE CONVENIÊNCIA ====================

    /**
     * Converte String de ações para Set<ModuleAction>
     */
    @Transient
    public Set<ModuleAction> getActions() {
        Set<ModuleAction> actions = new HashSet<>();
        if (actionsString != null && !actionsString.isEmpty()) {
            for (String actionName : actionsString.split(",")) {
                try {
                    actions.add(ModuleAction.valueOf(actionName.trim()));
                } catch (IllegalArgumentException e) {
                    // Ignora ações inválidas
                }
            }
        }
        return actions;
    }

    /**
     * Define ações a partir de Set<ModuleAction>
     */
    public void setActions(Set<ModuleAction> actions) {
        if (actions == null || actions.isEmpty()) {
            this.actionsString = "";
        } else {
            this.actionsString = String.join(",",
                actions.stream().map(Enum::name).toArray(String[]::new));
        }
    }

    /**
     * Verifica se possui uma ação específica
     */
    public boolean hasAction(ModuleAction action) {
        return getActions().contains(action);
    }

    /**
     * Verifica se possui acesso de leitura
     */
    public boolean canView() {
        return hasAction(ModuleAction.VIEW);
    }

    /**
     * Verifica se possui acesso de escrita
     */
    public boolean canWrite() {
        Set<ModuleAction> actions = getActions();
        return actions.contains(ModuleAction.CREATE) ||
               actions.contains(ModuleAction.EDIT) ||
               actions.contains(ModuleAction.DELETE);
    }

    /**
     * Verifica se possui acesso total
     */
    public boolean hasFullAccess() {
        Set<ModuleAction> actions = getActions();
        return actions.size() == ModuleAction.values().length;
    }
}

