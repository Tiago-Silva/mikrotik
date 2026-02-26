package br.com.mikrotik.features.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pré-visualização do parsing dos usuários PPPoE antes da sincronização completa")
public class ParsePreviewDTO {

    @Schema(description = "Total de usuários PPPoE analisados")
    private int total;

    @Schema(description = "Lista com o resultado do parsing de cada usuário")
    private List<ParsePreviewItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resultado do parsing de um único usuário PPPoE")
    public static class ParsePreviewItemDTO {

        @Schema(description = "Login PPPoE (username no MikroTik)", example = "elianabatista")
        private String pppoeUsername;

        @Schema(description = "Nome do cliente que seria criado", example = "Elianabatista")
        private String resolvedCustomerName;

        @Schema(description = "Comentário original do MikroTik", example = "rua 1 n120")
        private String originalComment;

        @Schema(description = "Rua/logradouro extraído do comentário", example = "Rua 1")
        private String parsedStreet;

        @Schema(description = "Número extraído do comentário", example = "120")
        private String parsedNumber;

        @Schema(description = "Bairro extraído do comentário")
        private String parsedNeighborhood;

        @Schema(description = "Telefone extraído do comentário")
        private String parsedPhone;

        @Schema(description = "Profile PPPoE atual", example = "PLANO-30M")
        private String profile;

        @Schema(description = "True se já existe contrato para este PPPoE no sistema")
        private boolean alreadySynced;

        @Schema(description = "Aviso de parsing (se houver)")
        private String warning;
    }
}

