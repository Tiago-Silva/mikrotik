package br.com.mikrotik.config;

import br.com.mikrotik.model.ApiUser;
import br.com.mikrotik.repository.ApiUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataInitializationConfig {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initializeData(ApiUserRepository apiUserRepository) {
        return args -> {
            // Verificar se usuários já existem
            if (apiUserRepository.findByUsername("admin").isEmpty()) {
                ApiUser admin = new ApiUser();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@example.com");
                admin.setRole("ADMIN");
                admin.setActive(true);
                apiUserRepository.save(admin);
                log.info("Usuário ADMIN criado com sucesso!");
            }

            if (apiUserRepository.findByUsername("operator").isEmpty()) {
                ApiUser operator = new ApiUser();
                operator.setUsername("operator");
                operator.setPassword(passwordEncoder.encode("operator123"));
                operator.setEmail("operator@example.com");
                operator.setRole("OPERATOR");
                operator.setActive(true);
                apiUserRepository.save(operator);
                log.info("Usuário OPERATOR criado com sucesso!");
            }

            if (apiUserRepository.findByUsername("viewer").isEmpty()) {
                ApiUser viewer = new ApiUser();
                viewer.setUsername("viewer");
                viewer.setPassword(passwordEncoder.encode("viewer123"));
                viewer.setEmail("viewer@example.com");
                viewer.setRole("VIEWER");
                viewer.setActive(true);
                apiUserRepository.save(viewer);
                log.info("Usuário VIEWER criado com sucesso!");
            }
        };
    }
}
