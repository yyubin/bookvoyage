package org.yyubin.infrastructure.external.book;

import java.time.Duration;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
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

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final KakaoBooksProperties properties;
    private final RestClient restClient;

    public KakaoBooksClient(KakaoBooksProperties properties) {
        this.properties = properties;

        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(CONNECT_TIMEOUT)
                .withReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(ClientHttpRequestFactories.get(settings))
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
