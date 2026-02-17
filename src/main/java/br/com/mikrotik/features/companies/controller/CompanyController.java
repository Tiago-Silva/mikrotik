package br.com.mikrotik.features.companies.controller;

import br.com.mikrotik.features.companies.dto.CompanyDTO;
import br.com.mikrotik.shared.dto.PageResponse;
import br.com.mikrotik.features.companies.service.CompanyService;
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

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Empresas", description = "Gerenciar empresas (Multi-tenant)")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar nova empresa", description = "Cria uma nova empresa no sistema multi-tenant")
    public ResponseEntity<CompanyDTO> create(@Valid @RequestBody CompanyDTO dto) {
        log.info("POST /api/companies - Criando nova empresa: {}", dto.getName());
        CompanyDTO created = companyService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar empresa por ID", description = "Retorna detalhes de uma empresa específica")
    public ResponseEntity<CompanyDTO> findById(@PathVariable Long id) {
        log.info("GET /api/companies/{} - Buscando empresa", id);
        CompanyDTO company = companyService.findById(id);
        return ResponseEntity.ok(company);
    }

    @GetMapping("/cnpj/{cnpj}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar empresa por CNPJ", description = "Retorna empresa pelo CNPJ")
    public ResponseEntity<CompanyDTO> findByCnpj(@PathVariable String cnpj) {
        log.info("GET /api/companies/cnpj/{} - Buscando empresa por CNPJ", cnpj);
        CompanyDTO company = companyService.findByCnpj(cnpj);
        return ResponseEntity.ok(company);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar todas as empresas", description = "Lista todas as empresas com paginação")
    public ResponseEntity<Page<CompanyDTO>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/companies - Listando empresas. Página: {}", pageable.getPageNumber());
        Page<CompanyDTO> companies = companyService.findAll(pageable);
        return ResponseEntity.ok(companies);
    }

    // Endpoint alternativo com estrutura JSON garantida
    @GetMapping("/v2")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "[V2] Listar todas as empresas", description = "Lista empresas com estrutura JSON estável")
    public ResponseEntity<PageResponse<CompanyDTO>> findAllV2(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/companies/v2 - Listando empresas (V2). Página: {}", pageable.getPageNumber());
        Page<CompanyDTO> companies = companyService.findAll(pageable);
        PageResponse<CompanyDTO> response = new PageResponse<>(companies);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar empresas ativas", description = "Lista apenas empresas ativas")
    public ResponseEntity<Page<CompanyDTO>> findByActive(
            @RequestParam(defaultValue = "true") Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/companies/active?active={} - Listando empresas", active);
        Page<CompanyDTO> companies = companyService.findByActive(active, pageable);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar empresas por nome", description = "Busca empresas por nome (parcial)")
    public ResponseEntity<Page<CompanyDTO>> findByName(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/companies/search?name={}", name);
        Page<CompanyDTO> companies = companyService.findByName(name, pageable);
        return ResponseEntity.ok(companies);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar empresa", description = "Atualiza os dados de uma empresa")
    public ResponseEntity<CompanyDTO> update(@PathVariable Long id, @Valid @RequestBody CompanyDTO dto) {
        log.info("PUT /api/companies/{} - Atualizando empresa", id);
        CompanyDTO updated = companyService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar empresa", description = "Desativa uma empresa (soft delete)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.info("PATCH /api/companies/{}/deactivate", id);
        companyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar empresa", description = "Ativa uma empresa desativada")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        log.info("PATCH /api/companies/{}/activate", id);
        companyService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar empresa", description = "Deleta permanentemente uma empresa (apenas se não houver dependências)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.warn("DELETE /api/companies/{} - Deletando empresa", id);
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
