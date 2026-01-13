package org.yyubin.infrastructure.external.book;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.yyubin.application.book.search.exception.ExternalBookSearchException;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchResponse;

@Component
public class KakaoBooksClient {

    private final KakaoBooksProperties properties;
    private final RestClient restClient;

    public KakaoBooksClient(KakaoBooksProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public KakaoBooksSearchResponse search(KakaoBooksSearchRequest request) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/v3/search/book")
                                .queryParam("query", request.query());

                        maybeAdd(uriBuilder, "sort", request.sort());
                        maybeAdd(uriBuilder, "page", request.page());
                        maybeAdd(uriBuilder, "size", request.size());
                        maybeAdd(uriBuilder, "target", request.target());

                        return uriBuilder.build();
                    })
                    .header("Authorization", "KakaoAK " + properties.getApiKey())
                    .retrieve()
                    .body(KakaoBooksSearchResponse.class);
        } catch (RestClientResponseException ex) {
            throw new ExternalBookSearchException(
                    "Kakao Books API responded with " + ex.getStatusCode(),
                    ex
            );
        } catch (RestClientException ex) {
            throw new ExternalBookSearchException("Failed to call Kakao Books API", ex);
        }
    }

    private void maybeAdd(UriBuilder builder, String name, Object value) {
        if (value != null) {
            if (value instanceof String str && !str.isBlank()) {
                builder.queryParam(name, value);
            } else if (!(value instanceof String)) {
                builder.queryParam(name, value);
            }
        }
    }
}
