package br.com.mikrotik.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Configuração para carregar variáveis do arquivo .env
 * Carrega automaticamente as variáveis de ambiente do arquivo .env na raiz do projeto
 */
@Configuration
public class DotEnvConfig {

    static {
        // Verificar se o arquivo .env existe e carregar manualmente
        File envFile = new File(".env");
        if (envFile.exists()) {
            try {
                Files.lines(Paths.get(".env"))
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(line -> {
                            String[] parts = line.split("=", 2);
                            if (parts.length == 2) {
                                String key = parts[0].trim();
                                String value = parts[1].trim();
                                // Remover aspas se existirem
                                if (value.startsWith("'") && value.endsWith("'")) {
                                    value = value.substring(1, value.length() - 1);
                                }
                                System.setProperty(key, value);
                            }
                        });
            } catch (IOException e) {
                System.err.println("Erro ao carregar arquivo .env: " + e.getMessage());
            }
        }
    }
}
