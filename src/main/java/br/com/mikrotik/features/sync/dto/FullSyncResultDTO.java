package br.com.mikrotik.features.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado detalhado da sincronização completa")
public class FullSyncResultDTO {

    // Profiles
    @Builder.Default
    @Schema(description = "Total de profiles encontrados no MikroTik")
    private Integer totalProfiles = 0;

    @Builder.Default
    @Schema(description = "Profiles sincronizados")
    private Integer syncedProfiles = 0;

    @Builder.Default
    @Schema(description = "Profiles ignorados (já existentes)")
    private Integer skippedProfiles = 0;

    // Service Plans
    @Builder.Default
    @Schema(description = "Planos de serviço criados automaticamente")
    private Integer createdServicePlans = 0;

    @Builder.Default
    @Schema(description = "Planos de serviço que já existiam")
    private Integer existingServicePlans = 0;

    // PPPoE Users
    @Builder.Default
    @Schema(description = "Total de usuários PPPoE encontrados no MikroTik")
    private Integer totalPppoeUsers = 0;

    @Builder.Default
    @Schema(description = "Usuários PPPoE sincronizados")
    private Integer syncedPppoeUsers = 0;

    @Builder.Default
    @Schema(description = "Usuários PPPoE ignorados (já existentes)")
    private Integer skippedPppoeUsers = 0;

    // Customers
    @Builder.Default
    @Schema(description = "Clientes criados automaticamente")
    private Integer createdCustomers = 0;

    @Builder.Default
    @Schema(description = "Clientes que já existiam")
    private Integer existingCustomers = 0;

    // Contracts
    @Builder.Default
    @Schema(description = "Contratos criados automaticamente")
    private Integer createdContracts = 0;

    @Builder.Default
    @Schema(description = "Contratos ativados automaticamente")
    private Integer activatedContracts = 0;

    @Builder.Default
    @Schema(description = "Contratos suspensos por profile bloqueado")
    private Integer suspendedContracts = 0;

    @Builder.Default
    @Schema(description = "Contratos que falharam")
    private Integer failedContracts = 0;

    // Details
    @Builder.Default
    @Schema(description = "Lista de nomes de profiles sincronizados")
    private List<String> syncedProfileNames = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Lista de planos de serviço criados")
    private List<String> createdServicePlanNames = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Lista de usernames PPPoE sincronizados")
    private List<String> syncedPppoeUsernames = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Lista de clientes criados")
    private List<String> createdCustomerNames = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Lista de contratos criados")
    private List<String> createdContractIds = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Mensagens de erro durante a sincronização")
    private List<String> errorMessages = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Avisos e informações adicionais")
    private List<String> warnings = new ArrayList<>();

    @Schema(description = "Tempo de execução em segundos")
    private Long executionTimeSeconds;

    @Schema(description = "Sincronização completada com sucesso")
    private Boolean success;
}

