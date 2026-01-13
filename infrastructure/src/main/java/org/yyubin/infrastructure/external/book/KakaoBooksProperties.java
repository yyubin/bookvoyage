package org.yyubin.infrastructure.external.book;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "kakao.books")
public class KakaoBooksProperties {

    /**
     * 카카오 REST API 키
     */
    private String apiKey;

    /**
     * 카카오 책 검색 엔드포인트
     */
    private String baseUrl = "https://dapi.kakao.com";

    /**
     * 페이지당 최대 결과 수 (최대 50)
     */
    private int maxResults = 50;
}
