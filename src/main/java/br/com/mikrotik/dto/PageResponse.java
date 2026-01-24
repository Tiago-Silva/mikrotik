package br.com.mikrotik.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO para encapsular respostas paginadas de forma consistente.
 * Garante estrutura JSON estável independente da implementação do Spring Data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    @JsonProperty("content")
    private List<T> content;

    @JsonProperty("page")
    private PageMetadata page;

    public PageResponse(Page<T> springPage) {
        this.content = springPage.getContent();
        this.page = new PageMetadata(
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMetadata {
        @JsonProperty("number")
        private int number;

        @JsonProperty("size")
        private int size;

        @JsonProperty("totalElements")
        private long totalElements;

        @JsonProperty("totalPages")
        private int totalPages;
    }
}
