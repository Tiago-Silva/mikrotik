package br.com.mikrotik.features.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoParseResult {

    /**
     * Nome do cliente extraído do comentário ou username
     */
    private String customerName;

    /**
     * Endereço extraído do comentário
     */
    private String address;

    /**
     * Número do endereço extraído
     */
    private String addressNumber;

    /**
     * Bairro extraído do comentário
     */
    private String neighborhood;

    /**
     * Telefone extraído do comentário (se encontrado)
     */
    private String phone;

    /**
     * Comentário original completo
     */
    private String originalComment;

    /**
     * Indica se o parsing foi bem-sucedido
     */
    private Boolean parseSuccess;

    /**
     * Mensagem de aviso se houver problemas no parsing
     */
    private String warningMessage;
}

