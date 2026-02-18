package br.com.mikrotik.shared.infrastructure.security;

import br.com.mikrotik.features.auth.model.ApiUser;
import br.com.mikrotik.features.auth.repository.ApiUserRepository;
import br.com.mikrotik.features.auth.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para validar acesso baseado em @RequireModuleAccess
 * Executa após autenticação JWT, antes do controller
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModuleAccessInterceptor implements HandlerInterceptor {

    private final PermissionService permissionService;
    private final ApiUserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Ignora se não for um método de controller
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Verifica se método tem annotation @RequireModuleAccess
        RequireModuleAccess methodAnnotation = handlerMethod.getMethodAnnotation(RequireModuleAccess.class);
        RequireModuleAccess classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireModuleAccess.class);

        RequireModuleAccess annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        // Se não tem annotation, permite acesso (usa @PreAuthorize ou outro mecanismo)
        if (annotation == null) {
            return true;
        }

        // Obter usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Tentativa de acesso sem autenticação: {} {}", request.getMethod(), request.getRequestURI());
            throw new AccessDeniedException("Usuário não autenticado");
        }

        // Extrair username do UserDetails
        String username;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else {
            username = authentication.getPrincipal().toString();
        }

        // Buscar usuário no banco
        ApiUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Usuário não encontrado"));

        // Verificar permissão usando PermissionService
        boolean hasAccess = permissionService.hasModuleAccess(
                user.getId(),
                annotation.module(),
                annotation.action()
        );

        if (!hasAccess) {
            log.warn("Acesso negado: usuário {} tentou acessar {} com ação {} em {} {}",
                    username,
                    annotation.module(),
                    annotation.action(),
                    request.getMethod(),
                    request.getRequestURI());

            throw new AccessDeniedException(
                    String.format("Você não tem permissão para %s no módulo %s",
                            annotation.action().getDisplayName(),
                            annotation.module().getDisplayName())
            );
        }

        log.debug("Acesso concedido: usuário {} acessou {} com {}",
                username, annotation.module(), annotation.action());
        return true;
    }
}

