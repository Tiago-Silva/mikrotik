package br.com.mikrotik.features.customers.controller;

import br.com.mikrotik.features.customers.dto.AddressDTO;
import br.com.mikrotik.features.customers.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Endereços", description = "Gerenciamento de endereços de clientes")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Criar novo endereço", description = "Cria um novo endereço para um cliente")
    public ResponseEntity<AddressDTO> create(@Valid @RequestBody AddressDTO dto) {
        log.info("POST /api/addresses - Criando endereço para cliente {}", dto.getCustomerId());
        AddressDTO created = addressService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar endereço por ID", description = "Retorna detalhes de um endereço específico")
    public ResponseEntity<AddressDTO> findById(@PathVariable Long id) {
        log.info("GET /api/addresses/{}", id);
        AddressDTO address = addressService.findById(id);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Listar endereços de um cliente", description = "Retorna todos os endereços de um cliente")
    public ResponseEntity<List<AddressDTO>> findByCustomer(@PathVariable Long customerId) {
        log.info("GET /api/addresses/customer/{}", customerId);
        List<AddressDTO> addresses = addressService.findByCustomer(customerId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/customer/{customerId}/installation")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar endereço de instalação", description = "Retorna o endereço de instalação principal do cliente")
    public ResponseEntity<AddressDTO> findInstallationAddress(@PathVariable Long customerId) {
        log.info("GET /api/addresses/customer/{}/installation", customerId);
        AddressDTO address = addressService.findInstallationAddress(customerId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/customer/{customerId}/billing")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'VIEWER')")
    @Operation(summary = "Buscar endereço de cobrança", description = "Retorna o endereço de cobrança do cliente")
    public ResponseEntity<AddressDTO> findBillingAddress(@PathVariable Long customerId) {
        log.info("GET /api/addresses/customer/{}/billing", customerId);
        AddressDTO address = addressService.findBillingAddress(customerId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Atualizar endereço", description = "Atualiza dados de um endereço existente")
    public ResponseEntity<AddressDTO> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        log.info("PUT /api/addresses/{}", id);
        AddressDTO updated = addressService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deletar endereço", description = "Remove um endereço do sistema")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/addresses/{}", id);
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
