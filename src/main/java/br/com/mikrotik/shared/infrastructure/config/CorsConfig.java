package br.com.mikrotik.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir origens do Lovable e localhost
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://*.lovable.app",
                "https://*.lovable.dev",
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://137.131.178.249",    // Seu IP da Oracle
                "http://137.131.178.249:*"   // Permite qualquer porta do seu IP (útil para testes)
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Headers permitidos (permitir todos os headers para flexibilidade)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Headers expostos ao cliente (para que o frontend possa lê-los)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "X-Company-Id",
                "x-company-id"
        ));

        // Permitir credenciais (cookies, authorization headers, etc)
        configuration.setAllowCredentials(true);

        // Tempo de cache da configuração CORS (em segundos)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
