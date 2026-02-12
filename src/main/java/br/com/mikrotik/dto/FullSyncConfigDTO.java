package br.com.mikrotik.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Configuração para sincronização completa do MikroTik")
public class FullSyncConfigDTO {

    @NotNull(message = "ID do servidor é obrigatório")
    @Schema(description = "ID do servidor MikroTik", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long serverId;

    @Builder.Default
    @Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 28")
    @Max(value = 28, message = "Dia de vencimento deve ser entre 1 e 28")
    @Schema(description = "Dia de vencimento padrão para novos contratos", example = "10")
    private Integer defaultBillingDay = 10;

    @Builder.Default
    @Schema(description = "Preço padrão para planos criados automaticamente", example = "50.00")
    private BigDecimal defaultPlanPrice = new BigDecimal("50.00");

    @Builder.Default
    @Schema(description = "Criar automaticamente planos de serviço para profiles sem plano", example = "true")
    private Boolean createMissingServicePlans = true;

    @Builder.Default
    @Schema(description = "Criar automaticamente clientes para usuários PPPoE sem cliente", example = "true")
    private Boolean createMissingCustomers = true;

    @Builder.Default
    @Schema(description = "Criar automaticamente contratos para usuários sincronizados", example = "true")
    private Boolean createContracts = true;

    @Builder.Default
    @Schema(description = "Ativar automaticamente os contratos criados", example = "true")
    private Boolean autoActivateContracts = true;
}

