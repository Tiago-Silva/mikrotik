package br.com.mikrotik.features.contracts.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.features.contracts.dto.ServicePlanDTO;
import br.com.mikrotik.features.contracts.service.ServicePlanService;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service-plans")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Planos de Serviço", description = "Gerenciamento de planos comerciais")
public class ServicePlanController {

    private final ServicePlanService servicePlanService;

    @PostMapping
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.CREATE)
    @Operation(summary = "Criar novo plano", description = "Cria um novo plano de serviço comercial")
    public ResponseEntity<ServicePlanDTO> create(@Valid @RequestBody ServicePlanDTO dto) {
        log.info("POST /api/service-plans - Criando novo plano: {}", dto.getName());
        ServicePlanDTO created = servicePlanService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar plano por ID", description = "Retorna detalhes de um plano específico")
    public ResponseEntity<ServicePlanDTO> findById(@PathVariable Long id) {
        log.info("GET /api/service-plans/{} - Buscando plano", id);
        ServicePlanDTO plan = servicePlanService.findById(id);
        return ResponseEntity.ok(plan);
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todos os planos", description = "Lista planos da empresa (paginado)")
    public ResponseEntity<Page<ServicePlanDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/service-plans - Listando planos");
        Page<ServicePlanDTO> plans = servicePlanService.findAll(pageable);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/active")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar planos ativos", description = "Lista planos ativos da empresa (paginado)")
    public ResponseEntity<Page<ServicePlanDTO>> findByActive(
            @RequestParam(defaultValue = "true") Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/service-plans/active?active={}", active);
        Page<ServicePlanDTO> plans = servicePlanService.findByActive(active, pageable);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/all")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todos", description = "Lista todos os planos da empresa (paginado)")
    public ResponseEntity<Page<ServicePlanDTO>> findAllByCompany(
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        log.info("GET /api/service-plans/all - Listando todos os planos");
        Page<ServicePlanDTO> plans = servicePlanService.findAllByCompany(pageable);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/active-list")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Listar ativos", description = "Lista planos ativos da empresa (paginado)")
    public ResponseEntity<Page<ServicePlanDTO>> findActiveByCompany(
            @PageableDefault(size = 50, sort = "name") Pageable pageable) {
        log.info("GET /api/service-plans/active-list - Listando planos ativos");
        Page<ServicePlanDTO> plans = servicePlanService.findActiveByCompany(pageable);
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/filter")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar com filtros", description = "Busca planos com múltiplos filtros opcionais")
    public ResponseEntity<Page<ServicePlanDTO>> findByFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/service-plans/filter - Buscando com filtros");
        Page<ServicePlanDTO> plans = servicePlanService.findByFilters(name, active, pageable);
        return ResponseEntity.ok(plans);
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar plano", description = "Atualiza os dados de um plano de serviço")
    public ResponseEntity<ServicePlanDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ServicePlanDTO dto) {
        log.info("PUT /api/service-plans/{} - Atualizando plano", id);
        ServicePlanDTO updated = servicePlanService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/active")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.EDIT)
    @Operation(summary = "Ativar/Desativar plano", description = "Altera o status ativo do plano")
    public ResponseEntity<ServicePlanDTO> toggleActive(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        log.info("PATCH /api/service-plans/{}/active?active={}", id, active);
        ServicePlanDTO updated = servicePlanService.toggleActive(id, active);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.DELETE)
    @Operation(summary = "Deletar plano", description = "Remove um plano de serviço do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/service-plans/{} - Deletando plano", id);
        servicePlanService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Contar planos", description = "Retorna o número total de planos da empresa")
    public ResponseEntity<Long> countByCompany() {
        log.info("GET /api/service-plans/count");
        long count = servicePlanService.countByCompany();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count-active")
    @RequireModuleAccess(module = SystemModule.SERVICE_PLANS, action = ModuleAction.VIEW)
    @Operation(summary = "Contar planos ativos", description = "Retorna o número de planos ativos da empresa")
    public ResponseEntity<Long> countActiveByCompany() {
        log.info("GET /api/service-plans/count-active");
        long count = servicePlanService.countActiveByCompany();
        return ResponseEntity.ok(count);
    }
}
