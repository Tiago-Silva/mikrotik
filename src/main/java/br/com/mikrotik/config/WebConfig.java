package br.com.mikrotik.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Configuração Web para suporte ao Spring Data Web.
 * Habilita a serialização estável de objetos Page via DTO.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {
    // A anotação @EnableSpringDataWebSupport com VIA_DTO garante uma estrutura JSON estável
    // para objetos Page retornados pelos controllers
}
