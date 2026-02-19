package br.com.mikrotik.features.network.server.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.shared.infrastructure.config.MikrotikConnectionConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mikrotik-status")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Status Mikrotik", description = "Verificar status e configuração do Mikrotik")
public class MikrotikStatusController {

    private final MikrotikConnectionConfig mikrotikConfig;

    /**
     * Obtém informações de configuração do Mikrotik
     */
    @GetMapping("/config")
    @Operation(summary = "Obter configuração", description = "Retorna informações de conexão com Mikrotik")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", mikrotikConfig.getHost());
        config.put("port", mikrotikConfig.getPort());
        config.put("username", mikrotikConfig.getUsername());
        config.put("timeout", mikrotikConfig.getTimeout());
        config.put("status", "Configurado");
        return ResponseEntity.ok(config);
    }

    /**
     * Testa a conexão com o Mikrotik
     */
    @GetMapping("/test-connection")
    @Operation(summary = "Testar conexão", description = "Verifica se consegue se conectar ao Mikrotik via SSH")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Testando conexão com Mikrotik em {}:{}",
                mikrotikConfig.getHost(), mikrotikConfig.getPort());

            boolean connected = mikrotikConfig.testConnection();

            response.put("success", connected);
            response.put("host", mikrotikConfig.getHost());
            response.put("port", mikrotikConfig.getPort());
            response.put("username", mikrotikConfig.getUsername());

            if (connected) {
                response.put("message", "✓ Conexão com Mikrotik estabelecida com sucesso!");
                response.put("status", "CONECTADO");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "✗ Falha ao conectar com Mikrotik");
                response.put("status", "DESCONECTADO");
                return ResponseEntity.status(503).body(response);
            }

        } catch (Exception e) {
            log.error("Erro ao testar conexão: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "✗ Erro: " + e.getMessage());
            response.put("status", "ERRO");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Valida a configuração
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar configuração", description = "Valida se a configuração do Mikrotik é válida")
    public ResponseEntity<Map<String, Object>> validateConfiguration() {
        Map<String, Object> response = new HashMap<>();

        try {
            mikrotikConfig.validateConfiguration();
            response.put("success", true);
            response.put("message", "✓ Configuração do Mikrotik é válida");
            response.put("config", mikrotikConfig.toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "✗ Erro na configuração: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Retorna status geral
     */
    @GetMapping("/status")
    @Operation(summary = "Status geral", description = "Retorna status geral de conexão com Mikrotik")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("mikrotik_host", mikrotikConfig.getHost());
        response.put("mikrotik_port", mikrotikConfig.getPort());
        response.put("application_status", "RODANDO");
        response.put("api_version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
