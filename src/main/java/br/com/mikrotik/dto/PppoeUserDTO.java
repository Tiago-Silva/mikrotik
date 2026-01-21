package br.com.mikrotik.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PppoeUserDTO {
    private Long id;

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    private String comment;

    private Boolean active = true;

    @NotNull(message = "Profile ID é obrigatório")
    private Long profileId;

    @NotNull(message = "Mikrotik Server ID é obrigatório")
    private Long mikrotikServerId;
}
