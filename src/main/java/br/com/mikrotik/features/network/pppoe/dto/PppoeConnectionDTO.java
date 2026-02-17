package br.com.mikrotik.features.network.pppoe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeConnectionDTO {
    private Long id;
    private Long userId;
    private String username;
    private String ipAddress;
    private String callingStationId;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
    private Long bytesUp;
    private Long bytesDown;
    private Boolean active;
    private Long mikrotikServerId;
}
