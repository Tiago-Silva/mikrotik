package br.com.mikrotik.dto;

import br.com.mikrotik.model.AutomationLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para logs de automação")
public class AutomationLogDTO {

    @Schema(description = "ID do log", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @Schema(description = "ID do contrato", example = "1")
    private Long contractId;

    @NotNull(message = "Tipo de ação é obrigatório")
    @Schema(description = "Tipo de ação executada", example = "BLOCK",
            allowableValues = {"BLOCK", "UNBLOCK", "REDUCE_SPEED", "SEND_WARNING", "SEND_EMAIL", "SEND_SMS"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private AutomationLog.ActionType actionType;

    @NotBlank(message = "Motivo é obrigatório")
    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    @Schema(description = "Motivo da ação", example = "Fatura vencida há 5 dias", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "Data e hora de execução", example = "2026-01-22T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime executedAt;

    @Schema(description = "Sucesso na execução", example = "true")
    private Boolean success;

    @Schema(description = "Mensagem de saída/erro", example = "Conexão PPPoE bloqueada com sucesso")
    private String outputMessage;

    // Campos informativos
    @Schema(description = "Nome do cliente")
    private String customerName;

    @Schema(description = "Username PPPoE")
    private String pppoeUsername;

    // Converter de Entity para DTO
    public static AutomationLogDTO fromEntity(AutomationLog log) {
        if (log == null) {
            return null;
        }

        AutomationLogDTOBuilder builder = AutomationLogDTO.builder()
                .id(log.getId())
                .companyId(log.getCompanyId())
                .contractId(log.getContractId())
                .actionType(log.getActionType())
                .reason(log.getReason())
                .executedAt(log.getExecutedAt())
                .success(log.getSuccess())
                .outputMessage(log.getOutputMessage());

        // Adicionar informações do contrato se disponível
        if (log.getContract() != null) {
            if (log.getContract().getCustomer() != null) {
                builder.customerName(log.getContract().getCustomer().getName());
            }
            if (log.getContract().getPppoeCredential() != null) {
                builder.pppoeUsername(log.getContract().getPppoeCredential().getUsername());
            }
        }

        return builder.build();
    }

    // Converter de DTO para Entity
    public AutomationLog toEntity() {
        return AutomationLog.builder()
                .id(this.id)
                .companyId(this.companyId)
                .contractId(this.contractId)
                .actionType(this.actionType)
                .reason(this.reason)
                .executedAt(this.executedAt)
                .success(this.success)
                .outputMessage(this.outputMessage)
                .build();
    }
}
