package org.yyubin.infrastructure.external.book;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.yyubin.application.book.search.exception.ExternalBookSearchException;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksVolumeResponse;

@Component
public class GoogleBooksClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final GoogleBooksProperties properties;
    private final RestClient restClient;

    public GoogleBooksClient(GoogleBooksProperties properties) {
        this.properties = properties;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public GoogleBooksVolumeResponse search(GoogleBooksSearchRequest request) {
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
