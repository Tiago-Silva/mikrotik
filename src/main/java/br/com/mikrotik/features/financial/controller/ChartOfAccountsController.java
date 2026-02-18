package br.com.mikrotik.features.financial.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.financial.dto.ChartOfAccountsDTO;
import br.com.mikrotik.features.financial.model.ChartOfAccounts;
import br.com.mikrotik.features.financial.service.ChartOfAccountsService;
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

import java.util.List;

@RestController
@RequestMapping("/api/chart-of-accounts")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Plano de Contas", description = "Gerenciamento do plano de contas (DRE)")
public class ChartOfAccountsController {

    private final ChartOfAccountsService chartOfAccountsService;

    @PostMapping
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.CREATE)
    @Operation(summary = "Criar conta", description = "Cria nova conta no plano de contas")
    public ResponseEntity<ChartOfAccountsDTO> create(@Valid @RequestBody ChartOfAccountsDTO dto) {
        log.info("POST /api/chart-of-accounts - Criando conta: {}", dto.getName());
        ChartOfAccountsDTO created = chartOfAccountsService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar por ID", description = "Retorna detalhes de uma conta")
    public ResponseEntity<ChartOfAccountsDTO> findById(@PathVariable Long id) {
        log.info("GET /api/chart-of-accounts/{}", id);
        ChartOfAccountsDTO account = chartOfAccountsService.findById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/code/{code}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar por código", description = "Retorna conta pelo código (ex: 1.1.01)")
    public ResponseEntity<ChartOfAccountsDTO> findByCode(@PathVariable String code) {
        log.info("GET /api/chart-of-accounts/code/{}", code);
        ChartOfAccountsDTO account = chartOfAccountsService.findByCode(code);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar contas", description = "Lista todas as contas (paginado)")
    public ResponseEntity<Page<ChartOfAccountsDTO>> findAll(
            @PageableDefault(size = 50, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/chart-of-accounts - Listando contas");
        Page<ChartOfAccountsDTO> accounts = chartOfAccountsService.findAll(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/all")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todas", description = "Lista todas as contas sem paginação")
    public ResponseEntity<List<ChartOfAccountsDTO>> findAllList() {
        log.info("GET /api/chart-of-accounts/all");
        List<ChartOfAccountsDTO> accounts = chartOfAccountsService.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/active")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar ativas", description = "Lista apenas contas ativas")
    public ResponseEntity<List<ChartOfAccountsDTO>> findActive() {
        log.info("GET /api/chart-of-accounts/active");
        List<ChartOfAccountsDTO> accounts = chartOfAccountsService.findActive();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/type/{accountType}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar por tipo", description = "Lista contas por tipo (REVENUE, EXPENSE, etc)")
    public ResponseEntity<Page<ChartOfAccountsDTO>> findByType(
            @PathVariable ChartOfAccounts.AccountType accountType,
            @PageableDefault(size = 50, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/chart-of-accounts/type/{}", accountType);
        Page<ChartOfAccountsDTO> accounts = chartOfAccountsService.findByType(accountType, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/category/{category}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar por categoria", description = "Lista contas por categoria")
    public ResponseEntity<Page<ChartOfAccountsDTO>> findByCategory(
            @PathVariable ChartOfAccounts.Category category,
            @PageableDefault(size = 50, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/chart-of-accounts/category/{}", category);
        Page<ChartOfAccountsDTO> accounts = chartOfAccountsService.findByCategory(category, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/parents")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar contas pai", description = "Lista contas de nível superior (sem parent)")
    public ResponseEntity<List<ChartOfAccountsDTO>> findParents() {
        log.info("GET /api/chart-of-accounts/parents");
        List<ChartOfAccountsDTO> accounts = chartOfAccountsService.findParentAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/children/{parentId}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar contas filhas", description = "Lista contas filhas de uma conta pai")
    public ResponseEntity<List<ChartOfAccountsDTO>> findChildren(@PathVariable Long parentId) {
        log.info("GET /api/chart-of-accounts/children/{}", parentId);
        List<ChartOfAccountsDTO> accounts = chartOfAccountsService.findChildAccounts(parentId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar conta", description = "Atualiza dados de uma conta")
    public ResponseEntity<ChartOfAccountsDTO> update(@PathVariable Long id, @Valid @RequestBody ChartOfAccountsDTO dto) {
        log.info("PUT /api/chart-of-accounts/{}", id);
        ChartOfAccountsDTO updated = chartOfAccountsService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.DELETE)
    @Operation(summary = "Inativar conta", description = "Inativa uma conta (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/chart-of-accounts/{}", id);
        chartOfAccountsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Contar contas", description = "Retorna total de contas")
    public ResponseEntity<Long> count() {
        log.info("GET /api/chart-of-accounts/count");
        long count = chartOfAccountsService.count();
        return ResponseEntity.ok(count);
    }
}

