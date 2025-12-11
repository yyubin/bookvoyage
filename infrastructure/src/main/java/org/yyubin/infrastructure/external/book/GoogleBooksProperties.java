package org.yyubin.infrastructure.external.book;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.books")
public class GoogleBooksProperties {

    /**
     * API 키는 필요 시에만 붙도록 옵션으로 둔다.
     */
    private String apiKey;

    /**
     * 기본 검색 엔드포인트.
     */
    private String baseUrl = "https://www.googleapis.com/books/v1/volumes";

    /**
     * maxResults는 40이 공식 상한.
     */
    private int maxResults = 40;
}
