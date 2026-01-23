package br.com.mikrotik.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private String username;
    private UserInfoDTO user;

    public LoginResponseDTO(String token, String type, Long expiresIn, String username) {
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
        this.username = username;
    }
}
