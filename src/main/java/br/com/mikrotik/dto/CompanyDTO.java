package br.com.mikrotik.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDTO {

    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    private String tradeName;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}",
             message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
    private String cnpj;

    @Email(message = "Email inválido")
    private String email;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{4,5}-\\d{4}",
             message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    private String supportPhone;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
