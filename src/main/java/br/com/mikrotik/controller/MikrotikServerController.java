package br.com.mikrotik.controller;

import br.com.mikrotik.dto.MikrotikServerDTO;
import br.com.mikrotik.service.MikrotikServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mikrotik-servers")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Servidores Mikrotik", description = "Gerenciar conexões com servidores Mikrotik")
public class MikrotikServerController {

    private final MikrotikServerService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo servidor", description = "Adicionar novo servidor Mikrotik à plataforma")
    public ResponseEntity<MikrotikServerDTO> create(@Valid @RequestBody MikrotikServerDTO dto) {
        log.info("Criando novo servidor Mikrotik: {}", dto.getName());
        MikrotikServerDTO created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter servidor por ID", description = "Retorna detalhes de um servidor Mikrotik específico")
    public ResponseEntity<MikrotikServerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os servidores", description = "Retorna lista de todos os servidores Mikrotik")
    public ResponseEntity<List<MikrotikServerDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar servidor", description = "Modificar dados de um servidor Mikrotik existente")
    public ResponseEntity<MikrotikServerDTO> update(@PathVariable Long id, @Valid @RequestBody MikrotikServerDTO dto) {
        log.info("Atualizando servidor Mikrotik: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar servidor", description = "Remover um servidor Mikrotik da plataforma")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deletando servidor Mikrotik: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/test-connection")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Testar conexão", description = "Verificar conectividade com o servidor Mikrotik")
    public ResponseEntity<Boolean> testConnection(@PathVariable Long id) {
        log.info("Testando conexão com servidor Mikrotik: {}", id);
        return ResponseEntity.ok(service.testConnection(id));
    }
}
