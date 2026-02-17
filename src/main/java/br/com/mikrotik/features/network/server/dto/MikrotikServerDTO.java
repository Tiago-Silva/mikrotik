package br.com.mikrotik.features.network.server.dto;

import br.com.mikrotik.features.network.server.model.MikrotikServer;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MikrotikServerDTO {
    private Long id;

    private Long companyId;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "IP Address é obrigatório")
    private String ipAddress;

    @NotNull(message = "Port é obrigatório")
    private Integer port;

    private Integer apiPort = 8728; // Porta API do Mikrotik (8728 sem SSL, 8729 com SSL)

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;

    private String description;

    private Boolean active = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastSyncAt;

    private MikrotikServer.SyncStatus syncStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
