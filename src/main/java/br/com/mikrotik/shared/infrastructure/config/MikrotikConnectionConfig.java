package br.com.mikrotik.shared.infrastructure.config;

import br.com.mikrotik.features.network.server.adapter.MikrotikSshService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de Conexão com Servidor Mikrotik
 *
 * Adicione no application.yml ou application.properties:
 *
 * mikrotik:
 *   host: 100.64.255.2
 *   port: 22
 *   username: admin
 *   password: sua_senha
 *   timeout: 30000
 */
@Configuration
@ConfigurationProperties(prefix = "mikrotik")
@Data
@RequiredArgsConstructor
@Slf4j
public class MikrotikConnectionConfig {

    private String host = "100.64.255.2";
    private Integer port = 22;
    private String username = "admin";
    private String password = "";
    private Integer timeout = 30000;

    private final MikrotikSshService mikrotikSshService;


    /**
     * Testa a conexão com o Mikrotik
     */
    public boolean testConnection() {
        try {
            log.info("Testando conexão com Mikrotik em {}:{}", host, port);
            boolean result = mikrotikSshService.testConnection(host, port, username, password);
            if (result) {
                log.info("✓ Conexão com Mikrotik estabelecida com sucesso!");
            } else {
                log.warn("✗ Falha ao conectar com Mikrotik");
            }
            return result;
        } catch (Exception e) {
            log.error("✗ Erro ao testar conexão: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida a configuração na inicialização
     */
    public void validateConfiguration() {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("mikrotik.host é obrigatório");
        }
        if (port == null || port <= 0 || port > 65535) {
            throw new IllegalArgumentException("mikrotik.port deve estar entre 1 e 65535");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("mikrotik.username é obrigatório");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("mikrotik.password é obrigatório");
        }
        log.info("Configuração Mikrotik validada: {}:{}", host, port);
    }
}
