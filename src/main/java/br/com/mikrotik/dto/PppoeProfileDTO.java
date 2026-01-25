package br.com.mikrotik.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeProfileDTO {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private String description;

    @NotNull(message = "Max Bitrate Download é obrigatório")
    private Long maxBitrateDl;

    @NotNull(message = "Max Bitrate Upload é obrigatório")
    private Long maxBitrateUl;

    @NotNull(message = "Session Timeout é obrigatório")
    private Integer sessionTimeout;

    private Boolean active = true;

    @NotNull(message = "Mikrotik Server ID é obrigatório")
    private Long mikrotikServerId;

    private String mikrotikServerName;
}
