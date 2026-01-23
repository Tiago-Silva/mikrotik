package br.com.mikrotik.dto;

import br.com.mikrotik.model.Address;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para endereços de clientes")
public class AddressDTO {

    @Schema(description = "ID do endereço", example = "1")
    private Long id;

    @Schema(description = "ID do cliente", example = "1")
    private Long customerId;

    @Schema(description = "Tipo de endereço", example = "BOTH", allowableValues = {"BILLING", "INSTALLATION", "BOTH"})
    private Address.AddressType type;

    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    @Schema(description = "CEP", example = "01310-100")
    private String zipCode;

    @Size(max = 255, message = "Logradouro deve ter no máximo 255 caracteres")
    @Schema(description = "Logradouro", example = "Avenida Paulista")
    private String street;

    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    @Schema(description = "Número", example = "1578")
    private String number;

    @Size(max = 255, message = "Complemento deve ter no máximo 255 caracteres")
    @Schema(description = "Complemento", example = "Apto 101")
    private String complement;

    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    @Schema(description = "Bairro", example = "Bela Vista")
    private String district;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    @Schema(description = "Cidade", example = "São Paulo")
    private String city;

    @Size(max = 2, message = "Estado deve ter 2 caracteres")
    @Schema(description = "Estado (UF)", example = "SP")
    private String state;

    @Schema(description = "Latitude", example = "-23.561684")
    private BigDecimal latitude;

    @Schema(description = "Longitude", example = "-46.655981")
    private BigDecimal longitude;

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Converter de Entity para DTO
    public static AddressDTO fromEntity(Address address) {
        if (address == null) {
            return null;
        }

        return AddressDTO.builder()
                .id(address.getId())
                .customerId(address.getCustomerId())
                .type(address.getType())
                .zipCode(address.getZipCode())
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .district(address.getDistrict())
                .city(address.getCity())
                .state(address.getState())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .createdAt(address.getCreatedAt())
                .build();
    }

    // Converter de DTO para Entity
    public Address toEntity() {
        return Address.builder()
                .id(this.id)
                .customerId(this.customerId)
                .type(this.type)
                .zipCode(this.zipCode)
                .street(this.street)
                .number(this.number)
                .complement(this.complement)
                .district(this.district)
                .city(this.city)
                .state(this.state)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .createdAt(this.createdAt)
                .build();
    }
}
