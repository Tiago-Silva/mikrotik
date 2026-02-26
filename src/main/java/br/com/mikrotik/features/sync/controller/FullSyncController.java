package br.com.mikrotik.features.sync.controller;

import br.com.mikrotik.features.auth.model.ModuleAction;
import br.com.mikrotik.features.auth.model.SystemModule;
import br.com.mikrotik.shared.infrastructure.security.RequireModuleAccess;

import br.com.mikrotik.features.sync.dto.FullSyncConfigDTO;
import br.com.mikrotik.features.sync.dto.FullSyncResultDTO;
import br.com.mikrotik.features.sync.dto.ParsePreviewDTO;
import br.com.mikrotik.features.sync.service.FullSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Sincronização Completa", description = "Sincronização completa do MikroTik (Profiles → Usuários → Clientes → Contratos)")
public class FullSyncController {

    private final FullSyncService fullSyncService;

    @PostMapping("/full-sync")
    @RequireModuleAccess(module = SystemModule.SYNC, action = ModuleAction.DELETE)
    @Operation(
        summary = "Sincronização completa automática",
        description = """
            Executa uma sincronização completa do MikroTik para o sistema:
            
            **FASES DA SINCRONIZAÇÃO:**
            
            1. **Profiles PPPoE** - Sincroniza todos os profiles do MikroTik
            2. **Planos de Serviço** - Cria automaticamente planos para cada profile
            3. **Usuários PPPoE** - Importa todos os usuários do MikroTik
            4. **Clientes** - Cria clientes automaticamente baseado nos comentários dos usuários PPPoE
            5. **Contratos** - Cria e ativa contratos vinculando Cliente + Plano + PPPoE
            
            **PARSING INTELIGENTE (baseado no padrão real do MikroTik):**
            - **Username (campo Name)** → nome do cliente (ex: `elianabatista` → `Elianabatista`)
            - **Comentário** → endereço/localização (ex: `rua 1 n120` → rua: `Rua 1`, nº: `120`)
            - Comentários com CPF são limpos automaticamente (CPF não é extraído como endereço)
            - Perfil `BLOQUEADO` → contrato criado como `SUSPENDED_FINANCIAL`
            
            **ANTES DE EXECUTAR:** Use `GET /api/sync/parse-preview/{serverId}` para visualizar
            como os dados serão interpretados sem persistir nada.
            
            **CONFIGURAÇÕES:**
            - `serverId`: ID do servidor MikroTik (obrigatório)
            - `defaultBillingDay`: Dia de vencimento padrão (1-28, padrão: 10)
            - `defaultPlanPrice`: Preço padrão para planos criados (padrão: R$ 50,00)
            - `createMissingServicePlans`: Criar planos automaticamente (padrão: true)
            - `createMissingCustomers`: Criar clientes automaticamente (padrão: true)
            - `createContracts`: Criar contratos automaticamente (padrão: true)
            - `autoActivateContracts`: Ativar contratos automaticamente (padrão: true)
            
            **IMPORTANTE:**
            - Usuários, clientes e contratos existentes são ignorados (não duplica)
            - Recomendado executar em horário de baixo movimento
            - Processo pode demorar alguns minutos dependendo da quantidade de usuários
            
            **EXEMPLOS DE PARSING:**
            - Username: `emersonmoura` + Comment: `rua 3 casinhas` → Nome: `Emersonmoura`, Rua: `Rua 3 Casinhas`
            - Username: `elisangelasilvalima` + Comment: `Rua Dr Alterives Marciel, 135 Bairro Bela Vista - CPF 95266413587`
              → Nome: `Elisangelasilvalima`, Rua: `Rua Dr Alterives Marciel`, Nº: `135`, Bairro: `Bela Vista`
            - Username: `eva` + Comment: (vazio) → Nome: `Eva`, sem endereço
            """
    )
    public ResponseEntity<FullSyncResultDTO> fullSync(@Valid @RequestBody FullSyncConfigDTO config) {
        log.info("==========================================================");
        log.info(">>> Requisição de SINCRONIZAÇÃO COMPLETA recebida <<<");
        log.info("Servidor ID: {}", config.getServerId());
        log.info("Dia vencimento: {}", config.getDefaultBillingDay());
        log.info("Preço padrão: R$ {}", config.getDefaultPlanPrice());
        log.info("Criar planos: {}", config.getCreateMissingServicePlans());
        log.info("Criar clientes: {}", config.getCreateMissingCustomers());
        log.info("Criar contratos: {}", config.getCreateContracts());
        log.info("Ativar contratos: {}", config.getAutoActivateContracts());
        log.info("==========================================================");

        FullSyncResultDTO result = fullSyncService.fullSync(config);

        log.info("==========================================================");
        log.info(">>> RESULTADO DA SINCRONIZAÇÃO COMPLETA <<<");
        log.info("Sucesso: {}", result.getSuccess());
        log.info("Tempo: {}s", result.getExecutionTimeSeconds());
        log.info("Profiles: {}/{} sincronizados", result.getSyncedProfiles(), result.getTotalProfiles());
        log.info("Planos criados: {}", result.getCreatedServicePlans());
        log.info("PPPoE sincronizados: {}/{}", result.getSyncedPppoeUsers(), result.getTotalPppoeUsers());
        log.info("Clientes criados: {}", result.getCreatedCustomers());
        log.info("Contratos criados: {}", result.getCreatedContracts());
        log.info("Contratos ativados: {}", result.getActivatedContracts());
        log.info("Erros: {}", result.getErrorMessages().size());
        log.info("Avisos: {}", result.getWarnings().size());
        log.info("==========================================================");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/parse-preview/{serverId}")
    @RequireModuleAccess(module = SystemModule.SYNC, action = ModuleAction.VIEW)
    @Operation(
        summary = "Pré-visualização do parsing PPPoE → Cliente",
        description = """
            **Operação somente leitura** — não persiste nada no banco.
            
            Mostra como cada usuário PPPoE do MikroTik seria interpretado pelo sistema:
            - `pppoeUsername`       → login PPPoE (campo Name no MikroTik)
            - `resolvedCustomerName`→ nome do cliente que seria criado
            - `originalComment`     → comentário original do MikroTik
            - `parsedStreet`        → rua extraída do comentário
            - `parsedNumber`        → número extraído do comentário
            - `parsedNeighborhood`  → bairro extraído do comentário
            - `alreadySynced`       → true se já existe contrato para este PPPoE
            - `warning`             → aviso de parsing (se houver)
            
            Use este endpoint **antes** do full-sync para validar os dados.
            """
    )
    public ResponseEntity<ParsePreviewDTO> parsePreview(@PathVariable Long serverId) {
        log.info("GET /api/sync/parse-preview/{} - Pré-visualização de parsing", serverId);
        return ResponseEntity.ok(fullSyncService.parsePreview(serverId));
    }
}

