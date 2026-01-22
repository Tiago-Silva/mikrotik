package br.com.mikrotik.controller;

import br.com.mikrotik.dto.LoginDTO;
import br.com.mikrotik.dto.LoginResponseDTO;
import br.com.mikrotik.model.ApiUser;
import br.com.mikrotik.repository.ApiUserRepository;
import br.com.mikrotik.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints de autenticação e autorização")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ApiUserRepository apiUserRepository;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autenticar usuário e obter JWT token")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        // Buscar usuário para obter companyId e role
        ApiUser user = apiUserRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Gerar token com companyId (multi-tenant)
        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        String token = tokenProvider.generateTokenWithCompany(
                user.getUsername(),
                user.getRole(),
                companyId
        );

        log.info("Usuário {} autenticado com sucesso (company: {})",
                loginDTO.getUsername(), companyId);

        return ResponseEntity.ok(new LoginResponseDTO(
                token,
                "Bearer",
                tokenProvider.getExpirationTime(),
                loginDTO.getUsername()
        ));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Verificar se o token JWT é válido")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        return ResponseEntity.ok(tokenProvider.validateToken(token));
    }
}
