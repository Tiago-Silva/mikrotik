package br.com.mikrotik.config;

import br.com.mikrotik.model.ApiUser;
import br.com.mikrotik.model.Company;
import br.com.mikrotik.repository.ApiUserRepository;
import br.com.mikrotik.repository.CompanyRepository;
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
    public CommandLineRunner initializeData(CompanyRepository companyRepository,
                                           ApiUserRepository apiUserRepository) {
        return args -> {
            // ==========================================
            // PASSO 1: Criar empresa padrão PRIMEIRO
            // ==========================================
            Company defaultCompany;

            if (companyRepository.count() == 0) {
                defaultCompany = new Company();
                defaultCompany.setName("Empresa Padrão");
                defaultCompany.setTradeName("ISP Default");
                defaultCompany.setCnpj("00.000.000/0001-00");
                defaultCompany.setEmail("admin@default.com");
                defaultCompany.setSupportPhone("(00) 0000-0000");
                defaultCompany.setActive(true);

                defaultCompany = companyRepository.save(defaultCompany);
                log.info("✅ Empresa padrão criada com sucesso! ID: {}", defaultCompany.getId());
            } else {
                defaultCompany = companyRepository.findById(1L)
                        .orElseGet(() -> companyRepository.findAll().get(0));
                log.info("✅ Empresa padrão já existe! ID: {}", defaultCompany.getId());
            }

            // ==========================================
            // PASSO 2: Criar usuários vinculados à empresa
            // ==========================================

            // Criar usuário ADMIN
            if (apiUserRepository.findByUsername("admin").isEmpty()) {
                ApiUser admin = new ApiUser();
                admin.setCompany(defaultCompany);  // ← VINCULA À EMPRESA
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));  // senha: admin
                admin.setEmail("admin@example.com");
                admin.setRole("ADMIN");
                admin.setActive(true);

                apiUserRepository.save(admin);
                log.info("✅ Usuário ADMIN criado com sucesso! (company_id: {})", defaultCompany.getId());
            } else {
                log.info("⚠️  Usuário ADMIN já existe");
            }

            // Criar usuário OPERATOR
            if (apiUserRepository.findByUsername("operator").isEmpty()) {
                ApiUser operator = new ApiUser();
                operator.setCompany(defaultCompany);  // ← VINCULA À EMPRESA
                operator.setUsername("operator");
                operator.setPassword(passwordEncoder.encode("operator"));  // senha: operator
                operator.setEmail("operator@example.com");
                operator.setRole("OPERATOR");
                operator.setActive(true);

                apiUserRepository.save(operator);
                log.info("✅ Usuário OPERATOR criado com sucesso! (company_id: {})", defaultCompany.getId());
            } else {
                log.info("⚠️  Usuário OPERATOR já existe");
            }

            // Criar usuário VIEWER
            if (apiUserRepository.findByUsername("viewer").isEmpty()) {
                ApiUser viewer = new ApiUser();
                viewer.setCompany(defaultCompany);  // ← VINCULA À EMPRESA
                viewer.setUsername("viewer");
                viewer.setPassword(passwordEncoder.encode("viewer"));  // senha: viewer
                viewer.setEmail("viewer@example.com");
                viewer.setRole("VIEWER");
                viewer.setActive(true);

                apiUserRepository.save(viewer);
                log.info("✅ Usuário VIEWER criado com sucesso! (company_id: {})", defaultCompany.getId());
            } else {
                log.info("⚠️  Usuário VIEWER já existe");
            }

            log.info("==========================================");
            log.info("✅ Inicialização de dados concluída!");
            log.info("   Empresa: {} (ID: {})", defaultCompany.getName(), defaultCompany.getId());
            log.info("   Usuários: {} registros", apiUserRepository.count());
            log.info("==========================================");
        };
    }
}
