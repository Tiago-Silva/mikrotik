package br.com.mikrotik.dto;

import br.com.mikrotik.model.PppoeUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeUserDTO {
    private Long id;

    private Long companyId;

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;

    @Email(message = "Email deve ser válido")
    private String email;

    private String comment;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "MAC address inválido")
    private String macAddress;

    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "IP inválido")
    private String staticIp;

    private PppoeUser.UserStatus status;

    private Boolean active = true;

    private LocalDateTime lastConnectionAt;

    @NotNull(message = "Profile ID é obrigatório")
    private Long profileId;

    @NotNull(message = "Mikrotik Server ID é obrigatório")
    private Long mikrotikServerId;
}
