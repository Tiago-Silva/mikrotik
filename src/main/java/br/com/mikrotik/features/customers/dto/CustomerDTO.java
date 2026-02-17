package br.com.mikrotik.features.customers.dto;

import br.com.mikrotik.features.customers.model.Address;
import br.com.mikrotik.features.customers.model.Customer;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para informações de clientes")
public class CustomerDTO {

    @Schema(description = "ID do cliente", example = "1")
    private Long id;

    @Schema(description = "ID da empresa", example = "1")
    private Long companyId;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 255, message = "Nome deve ter entre 3 e 255 caracteres")
    @Schema(description = "Nome do cliente", example = "João Silva", required = true)
    private String name;

    @NotNull(message = "Tipo de cliente é obrigatório")
    @Schema(description = "Tipo de pessoa", example = "FISICA", required = true, allowableValues = {"FISICA", "JURIDICA"})
    private Customer.CustomerType type;

    @NotBlank(message = "Documento é obrigatório")
    @Size(max = 20, message = "Documento deve ter no máximo 20 caracteres")
    @Schema(description = "CPF ou CNPJ", example = "123.456.789-00", required = true)
    private String document;

    @Size(max = 20, message = "RG/IE deve ter no máximo 20 caracteres")
    @Schema(description = "RG ou Inscrição Estadual", example = "12.345.678-9")
    private String rgIe;

    @Email(message = "Email inválido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    @Schema(description = "Email do cliente", example = "joao@example.com")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Schema(description = "Telefone principal", example = "(11) 98765-4321")
    private String phonePrimary;

    @Size(max = 20, message = "WhatsApp deve ter no máximo 20 caracteres")
    @Schema(description = "Telefone WhatsApp", example = "(11) 98765-4321")
    private String phoneWhatsapp;

    @Schema(description = "Status do cliente", example = "ACTIVE", allowableValues = {"ACTIVE", "SUSPENDED", "CANCELED", "PROSPECT"})
    private Customer.CustomerStatus status;

    @Schema(description = "Observações sobre o cliente")
    private String notes;

    @Schema(description = "Endereços do cliente")
    @Builder.Default
    private List<AddressDTO> addresses = new ArrayList<>();

    @Schema(description = "Data de criação")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // Converter de Entity para DTO
    public static CustomerDTO fromEntity(Customer customer) {
        if (customer == null) {
            return null;
        }

        List<AddressDTO> addressDTOs = customer.getAddresses() != null
                ? customer.getAddresses().stream()
                .map(AddressDTO::fromEntity)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return CustomerDTO.builder()
                .id(customer.getId())
                .companyId(customer.getCompanyId())
                .name(customer.getName())
                .type(customer.getType())
                .document(customer.getDocument())
                .rgIe(customer.getRgIe())
                .email(customer.getEmail())
                .phonePrimary(customer.getPhonePrimary())
                .phoneWhatsapp(customer.getPhoneWhatsapp())
                .status(customer.getStatus())
                .notes(customer.getNotes())
                .addresses(addressDTOs)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    // Converter de DTO para Entity
    public Customer toEntity() {
        Customer customer = Customer.builder()
                .id(this.id)
                .companyId(this.companyId)
                .name(this.name)
                .type(this.type)
                .document(this.document)
                .rgIe(this.rgIe)
                .email(this.email)
                .phonePrimary(this.phonePrimary)
                .phoneWhatsapp(this.phoneWhatsapp)
                .status(this.status)
                .notes(this.notes)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();

        if (this.addresses != null && !this.addresses.isEmpty()) {
            List<Address> addressEntities = this.addresses.stream()
                    .map(AddressDTO::toEntity)
                    .collect(Collectors.toList());
            customer.setAddresses(addressEntities);
        }

        return customer;
    }
}
