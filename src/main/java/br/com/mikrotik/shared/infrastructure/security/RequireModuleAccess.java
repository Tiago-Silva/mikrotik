package br.com.mikrotik.shared.infrastructure.security;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation para controle de acesso granular por módulo
 * Substitui @PreAuthorize para controle baseado em permissões customizadas
 *
 * Uso:
 * @RequireModuleAccess(module = SystemModule.CUSTOMERS, action = ModuleAction.VIEW)
 * @RequireModuleAccess(module = SystemModule.CONTRACTS, action = ModuleAction.CREATE)
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireModuleAccess {

    /**
     * Módulo do sistema que requer acesso
     */
    SystemModule module();

    /**
     * Ação que o usuário precisa ter permissão para executar
     */
    ModuleAction action();

    /**
     * Mensagem customizada de erro (opcional)
     */
    String message() default "Acesso negado ao módulo";
}

