package br.com.mikrotik.controller;

import br.com.mikrotik.dto.AutomationLogDTO;
import br.com.mikrotik.model.AutomationLog;
import br.com.mikrotik.service.AutomationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automation-logs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Logs de Automação", description = "Gerenciamento de logs de ações automáticas do sistema")
public class AutomationLogController {

    private final AutomationLogService automationLogService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar log de automação", description = "Registra uma nova ação automática")
    public ResponseEntity<AutomationLogDTO> create(@Valid @RequestBody AutomationLogDTO dto) {
        log.info("POST /api/automation-logs - Criando log de automação");
        AutomationLogDTO created = automationLogService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar log por ID", description = "Retorna detalhes de um log específico")
    public ResponseEntity<AutomationLogDTO> findById(@PathVariable Long id) {
        log.info("GET /api/automation-logs/{} - Buscando log", id);
        AutomationLogDTO automationLog = automationLogService.findById(id);
        return ResponseEntity.ok(automationLog);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todos os logs", description = "Lista logs de automação da empresa (paginado)")
    public ResponseEntity<Page<AutomationLogDTO>> findAll(
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/automation-logs - Listando logs");
        Page<AutomationLogDTO> logs = automationLogService.findAll(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/contract/{contractId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar logs por contrato", description = "Lista logs de um contrato específico")
    public ResponseEntity<Page<AutomationLogDTO>> findByContract(
            @PathVariable Long contractId,
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/automation-logs/contract/{} - Listando logs", contractId);
        Page<AutomationLogDTO> logs = automationLogService.findByContract(contractId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action-type/{actionType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar logs por tipo", description = "Lista logs filtrados por tipo de ação")
    public ResponseEntity<Page<AutomationLogDTO>> findByActionType(
            @PathVariable AutomationLog.ActionType actionType,
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/automation-logs/action-type/{} - Listando logs", actionType);
        Page<AutomationLogDTO> logs = automationLogService.findByActionType(actionType, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar logs recentes", description = "Retorna os logs mais recentes (paginado)")
    public ResponseEntity<Page<AutomationLogDTO>> findRecent(
            @PageableDefault(size = 100, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/automation-logs/recent - Listando logs recentes");
        Page<AutomationLogDTO> logs = automationLogService.findRecent(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar com filtros", description = "Busca logs com múltiplos filtros opcionais")
    public ResponseEntity<Page<AutomationLogDTO>> findByFilters(
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) AutomationLog.ActionType actionType,
            @RequestParam(required = false) Boolean success,
            @PageableDefault(size = 20, sort = "executedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/automation-logs/filter - Buscando com filtros");
        Page<AutomationLogDTO> logs = automationLogService.findByFilters(contractId, actionType, success, pageable);
        return ResponseEntity.ok(logs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar log", description = "Remove um log de automação do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/automation-logs/{} - Deletando log", id);
        automationLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar logs", description = "Retorna o número total de logs da empresa")
    public ResponseEntity<Long> countByCompany() {
        log.info("GET /api/automation-logs/count");
        long count = automationLogService.countByCompany();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/action-type/{actionType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar por tipo", description = "Retorna o número de logs por tipo de ação")
    public ResponseEntity<Long> countByActionType(@PathVariable AutomationLog.ActionType actionType) {
        log.info("GET /api/automation-logs/count/action-type/{}", actionType);
        long count = automationLogService.countByActionType(actionType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/successful")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar sucessos", description = "Retorna o número de logs com sucesso")
    public ResponseEntity<Long> countSuccessful() {
        log.info("GET /api/automation-logs/count/successful");
        long count = automationLogService.countSuccessful();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Contar falhas", description = "Retorna o número de logs com falha")
    public ResponseEntity<Long> countFailed() {
        log.info("GET /api/automation-logs/count/failed");
        long count = automationLogService.countFailed();
        return ResponseEntity.ok(count);
    }
}
