package org.yyubin.infrastructure.external.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoBooksMetaResponse(
        @JsonProperty("total_count")
        Integer totalCount,

        @JsonProperty("pageable_count")
        Integer pageableCount,

        @JsonProperty("is_end")
        Boolean isEnd
) {
}
