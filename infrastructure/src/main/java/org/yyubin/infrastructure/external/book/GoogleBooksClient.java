package org.yyubin.infrastructure.external.book;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.yyubin.application.book.search.exception.ExternalBookSearchException;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksVolumeResponse;

@Component
@RequiredArgsConstructor
public class GoogleBooksClient {

    private final RestClient.Builder restClientBuilder;
    private final GoogleBooksProperties properties;

    public GoogleBooksVolumeResponse search(GoogleBooksSearchRequest request) {
        RestClient restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();

        try {
            return restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .queryParam("q", request.query())
                                .queryParam("startIndex", request.startIndex())
                                .queryParam("maxResults", request.maxResults());

                        maybeAdd(uriBuilder, "langRestrict", request.language());
                        maybeAdd(uriBuilder, "orderBy", request.orderBy());
                        maybeAdd(uriBuilder, "printType", request.printType());
                        maybeAdd(uriBuilder, "key", properties.getApiKey());
                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(GoogleBooksVolumeResponse.class);
        } catch (RestClientResponseException ex) {
            throw new ExternalBookSearchException(
                    "Google Books API responded with " + ex.getStatusCode(),
                    ex
            );
        } catch (RestClientException ex) {
            throw new ExternalBookSearchException("Failed to call Google Books API", ex);
        }
    }

    private void maybeAdd(UriBuilder builder, String name, String value) {
        if (value != null && !value.isBlank()) {
            builder.queryParam(name, value);
        }
    }
}
