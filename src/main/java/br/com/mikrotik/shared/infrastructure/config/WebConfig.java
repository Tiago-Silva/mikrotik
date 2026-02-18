package br.com.mikrotik.shared.infrastructure.config;

import br.com.mikrotik.shared.infrastructure.security.ModuleAccessInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração Web para suporte ao Spring Data Web e interceptors de segurança.
 * Habilita a serialização estável de objetos Page via DTO.
 * Registra interceptor de controle de acesso por módulo.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ModuleAccessInterceptor moduleAccessInterceptor;

    /**
     * Registra interceptors customizados
     * ModuleAccessInterceptor: valida @RequireModuleAccess após autenticação JWT
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(moduleAccessInterceptor)
                .addPathPatterns("/api/**") // Aplica a todas as rotas da API
                .excludePathPatterns(
                        "/api/auth/**",      // Exclui rotas de autenticação
                        "/swagger-ui/**",    // Exclui Swagger
                        "/v3/api-docs/**"    // Exclui OpenAPI docs
                );
    }
}
