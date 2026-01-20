package org.yyubin.infrastructure.external.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.yyubin.application.book.search.exception.ExternalBookSearchException;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksVolumeResponse;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleBooksClient 테스트")
class GoogleBooksClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private GoogleBooksProperties properties;
    private GoogleBooksClient client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new GoogleBooksProperties();
        properties.setBaseUrl("https://www.googleapis.com/books/v1/volumes");
        properties.setApiKey("test-api-key");

        client = new GoogleBooksClient(properties);
        ReflectionTestUtils.setField(client, "restClient", restClient);
    }

    @Test
    @DisplayName("검색 요청을 성공적으로 수행한다")
    @SuppressWarnings("unchecked")
    void search_Success() {
        // Given
        GoogleBooksSearchRequest request = new GoogleBooksSearchRequest(
                "테스트", 0, 10, "ko", "relevance", "books"
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(10, List.of());

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(GoogleBooksVolumeResponse.class)).thenReturn(response);

        // When
        GoogleBooksVolumeResponse result = client.search(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalItems()).isEqualTo(10);
    }

    @Test
    @DisplayName("4xx 응답 시 ExternalBookSearchException을 던진다")
    @SuppressWarnings("unchecked")
    void search_4xxResponse_ThrowsException() {
        // Given
        GoogleBooksSearchRequest request = new GoogleBooksSearchRequest(
                "테스트", 0, 10, null, null, null
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(
                new RestClientResponseException("Bad Request", HttpStatusCode.valueOf(400), "Bad Request", null, null, null)
        );

        // When & Then
        assertThatThrownBy(() -> client.search(request))
                .isInstanceOf(ExternalBookSearchException.class)
                .hasMessageContaining("Google Books API responded with");
    }

    @Test
    @DisplayName("5xx 응답 시 ExternalBookSearchException을 던진다")
    @SuppressWarnings("unchecked")
    void search_5xxResponse_ThrowsException() {
        // Given
        GoogleBooksSearchRequest request = new GoogleBooksSearchRequest(
                "테스트", 0, 10, null, null, null
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(
                new RestClientResponseException("Internal Server Error", HttpStatusCode.valueOf(500), "Internal Server Error", null, null, null)
        );

        // When & Then
        assertThatThrownBy(() -> client.search(request))
                .isInstanceOf(ExternalBookSearchException.class)
                .hasMessageContaining("Google Books API responded with");
    }

    @Test
    @DisplayName("null 응답을 처리한다")
    @SuppressWarnings("unchecked")
    void search_NullResponse() {
        // Given
        GoogleBooksSearchRequest request = new GoogleBooksSearchRequest(
                "테스트", 0, 10, null, null, null
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(GoogleBooksVolumeResponse.class)).thenReturn(null);

        // When
        GoogleBooksVolumeResponse result = client.search(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("요청 파라미터가 올바르게 설정된다")
    void search_RequestParametersAreSet() {
        // Given
        GoogleBooksSearchRequest request = new GoogleBooksSearchRequest(
                "테스트 검색어", 10, 20, "ko", "newest", "books"
        );

        // Then - Request 객체가 올바르게 생성되었는지 확인
        assertThat(request.query()).isEqualTo("테스트 검색어");
        assertThat(request.startIndex()).isEqualTo(10);
        assertThat(request.maxResults()).isEqualTo(20);
        assertThat(request.language()).isEqualTo("ko");
        assertThat(request.orderBy()).isEqualTo("newest");
        assertThat(request.printType()).isEqualTo("books");
    }
}
