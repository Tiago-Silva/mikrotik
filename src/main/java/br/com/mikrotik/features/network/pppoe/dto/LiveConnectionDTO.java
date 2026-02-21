package br.com.mikrotik.features.network.pppoe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Dados da conexão ativa de um cliente consultados diretamente no Mikrotik")
public class LiveConnectionDTO {

    @Schema(description = "ID do cliente no sistema", example = "42")
    private Long customerId;

    @Schema(description = "Nome do cliente", example = "João da Silva")
    private String customerName;

    @Schema(description = "Username PPPoE", example = "joaosilva")
    private String pppoeUsername;

    @Schema(description = "IP remoto atribuído ao cliente", example = "10.10.1.55")
    private String remoteAddress;

    @Schema(description = "IP local do concentrador", example = "10.10.0.1")
    private String localAddress;

    @Schema(description = "MAC / Calling Station ID", example = "AA:BB:CC:DD:EE:FF")
    private String callingStationId;

    @Schema(description = "Tempo de uptime da sessão", example = "3h25m10s")
    private String uptime;

    @Schema(description = "Serviço PPPoE (interface)", example = "pppoe-isp")
    private String service;

    @Schema(description = "Se a conexão está ativa no Mikrotik neste momento", example = "true")
    private boolean online;

    @Schema(description = "Mensagem informativa (ex: motivo de offline)")
    private String message;
}

