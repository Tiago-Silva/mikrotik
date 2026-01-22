package br.com.mikrotik.security;

import br.com.mikrotik.util.CompanyContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para extrair o companyId do token JWT e popular o CompanyContextHolder.
 * Executado antes do JwtAuthenticationFilter para disponibilizar o contexto da empresa
 * em toda a requisição.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Executar antes de outros filtros
public class CompanyContextFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long companyId = jwtTokenProvider.getCompanyId(token);
                if (companyId != null) {
                    CompanyContextHolder.setCompanyId(companyId);
                    log.debug("CompanyId {} definido no contexto da requisição", companyId);
                } else {
                    log.debug("Token sem companyId (backward compatibility)");
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Sempre limpar o ThreadLocal para evitar memory leaks
            CompanyContextHolder.clear();
        }
    }
}
