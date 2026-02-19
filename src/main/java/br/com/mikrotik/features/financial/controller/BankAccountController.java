package br.com.mikrotik.features.financial.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.financial.dto.BankAccountDTO;
import br.com.mikrotik.features.financial.model.BankAccount;
import br.com.mikrotik.features.financial.service.BankAccountService;
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
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Contas Bancárias", description = "Gerenciamento de contas bancárias e caixas")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.CREATE)
    @Operation(summary = "Criar conta bancária", description = "Cria nova conta bancária ou caixa")
    public ResponseEntity<BankAccountDTO> create(@Valid @RequestBody BankAccountDTO dto) {
        log.info("POST /api/bank-accounts - Criando conta: {}", dto.getName());
        BankAccountDTO created = bankAccountService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Buscar conta por ID", description = "Retorna detalhes de uma conta bancária")
    public ResponseEntity<BankAccountDTO> findById(@PathVariable Long id) {
        log.info("GET /api/bank-accounts/{}", id);
        BankAccountDTO account = bankAccountService.findById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar contas", description = "Lista todas as contas bancárias (paginado)")
    public ResponseEntity<Page<BankAccountDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/bank-accounts - Listando contas");
        Page<BankAccountDTO> accounts = bankAccountService.findAll(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/all")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar todas as contas", description = "Lista todas as contas sem paginação")
    public ResponseEntity<List<BankAccountDTO>> findAllList() {
        log.info("GET /api/bank-accounts/all");
        List<BankAccountDTO> accounts = bankAccountService.findAll();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/active")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar contas ativas", description = "Lista apenas contas ativas")
    public ResponseEntity<Page<BankAccountDTO>> findActive(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/bank-accounts/active");
        Page<BankAccountDTO> accounts = bankAccountService.findActive(pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/type/{accountType}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Listar por tipo", description = "Lista contas por tipo (CHECKING, CASH, etc)")
    public ResponseEntity<Page<BankAccountDTO>> findByType(
            @PathVariable BankAccount.AccountType accountType,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/bank-accounts/type/{}", accountType);
        Page<BankAccountDTO> accounts = bankAccountService.findByType(accountType, pageable);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.EDIT)
    @Operation(summary = "Atualizar conta", description = "Atualiza dados de uma conta bancária")
    public ResponseEntity<BankAccountDTO> update(@PathVariable Long id, @Valid @RequestBody BankAccountDTO dto) {
        log.info("PUT /api/bank-accounts/{}", id);
        BankAccountDTO updated = bankAccountService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.DELETE)
    @Operation(summary = "Inativar conta", description = "Inativa uma conta bancária (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/bank-accounts/{}", id);
        bankAccountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @RequireModuleAccess(module = SystemModule.FINANCIAL, action = ModuleAction.VIEW)
    @Operation(summary = "Contar contas", description = "Retorna total de contas bancárias")
    public ResponseEntity<Long> count() {
        log.info("GET /api/bank-accounts/count");
        long count = bankAccountService.count();
        return ResponseEntity.ok(count);
    }
}

