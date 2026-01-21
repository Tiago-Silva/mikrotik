package br.com.mikrotik.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MikrotikPppoeProfileDTO {
    private String name;
    private String localAddress;
    private String remoteAddress;
    private String rateLimit;
    private String sessionTimeout;
    private Boolean disabled;
    private String comment;
}
