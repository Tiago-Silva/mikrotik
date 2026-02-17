package br.com.mikrotik.features.sync.controller;

import br.com.mikrotik.features.sync.dto.FullSyncConfigDTO;
import br.com.mikrotik.features.sync.dto.FullSyncResultDTO;
import br.com.mikrotik.features.sync.service.FullSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
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
            
            **PARSING INTELIGENTE:**
            - Extrai nome do cliente do comentário ou username
            - Identifica endereço (rua, travessa, avenida + número)
            - Cria endereço de instalação automaticamente
            
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
            
            **EXEMPLO DE COMENTÁRIO PARSEADO:**
            - "felipe achy/ nalmar alcantara n255" → Nome: "Felipe Achy", Endereço: "Nalmar Alcantara", Número: "255"
            - "rua7 n128" → Nome do username, Endereço: "Rua7", Número: "128"
            - Sem comentário → Usa username como nome do cliente
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
}

