package org.yyubin.infrastructure.external.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoBooksDocumentResponse(
        String title,
        String contents,
        String url,
        String isbn,
        String datetime,
        List<String> authors,
        List<String> translators,
        String publisher,

        @JsonProperty("price")
        Integer price,

        @JsonProperty("sale_price")
        Integer salePrice,

        String thumbnail,
        String status
) {
}
