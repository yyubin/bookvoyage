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
import org.yyubin.infrastructure.external.book.dto.KakaoBooksMetaResponse;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchResponse;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoBooksClient 테스트")
class KakaoBooksClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private KakaoBooksProperties properties;
    private KakaoBooksClient client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        properties = new KakaoBooksProperties();
        properties.setBaseUrl("https://dapi.kakao.com");
        properties.setApiKey("test-kakao-api-key");

        client = new KakaoBooksClient(properties);
        ReflectionTestUtils.setField(client, "restClient", restClient);
    }

    @Test
    @DisplayName("검색 요청을 성공적으로 수행한다")
    @SuppressWarnings("unchecked")
    void search_Success() {
        // Given
        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                "테스트", "accuracy", 1, 10, null
        );

        KakaoBooksSearchResponse response = new KakaoBooksSearchResponse(
                new KakaoBooksMetaResponse(10, 10, false),
                List.of()
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoBooksSearchResponse.class)).thenReturn(response);

        // When
        KakaoBooksSearchResponse result = client.search(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.meta().totalCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Authorization 헤더가 설정된다")
    @SuppressWarnings("unchecked")
    void search_AuthorizationHeaderSet() {
        // Given
        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                "테스트", null, 1, 10, null
        );

        KakaoBooksSearchResponse response = new KakaoBooksSearchResponse(
                new KakaoBooksMetaResponse(0, 0, true),
                List.of()
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoBooksSearchResponse.class)).thenReturn(response);

        // When
        client.search(request);

        // Then
        verify(requestHeadersUriSpec).header("Authorization", "KakaoAK test-kakao-api-key");
    }

    @Test
    @DisplayName("4xx 응답 시 ExternalBookSearchException을 던진다")
    @SuppressWarnings("unchecked")
    void search_4xxResponse_ThrowsException() {
        // Given
        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                "테스트", null, 1, 10, null
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(
                new RestClientResponseException("Unauthorized", HttpStatusCode.valueOf(401), "Unauthorized", null, null, null)
        );

        // When & Then
        assertThatThrownBy(() -> client.search(request))
                .isInstanceOf(ExternalBookSearchException.class)
                .hasMessageContaining("Kakao Books API responded with");
    }

    @Test
    @DisplayName("5xx 응답 시 ExternalBookSearchException을 던진다")
    @SuppressWarnings("unchecked")
    void search_5xxResponse_ThrowsException() {
        // Given
        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                "테스트", null, 1, 10, null
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(
                new RestClientResponseException("Internal Server Error", HttpStatusCode.valueOf(500), "Internal Server Error", null, null, null)
        );

        // When & Then
        assertThatThrownBy(() -> client.search(request))
                .isInstanceOf(ExternalBookSearchException.class)
                .hasMessageContaining("Kakao Books API responded with");
    }

    @Test
    @DisplayName("null 응답을 처리한다")
    @SuppressWarnings("unchecked")
    void search_NullResponse() {
        // Given
        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                "테스트", null, 1, 10, null
        );

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(anyString(), anyString())).thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(KakaoBooksSearchResponse.class)).thenReturn(null);

        // When
        KakaoBooksSearchResponse result = client.search(request);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("요청 파라미터가 올바르게 설정된다")
    void search_RequestParametersAreSet() {
        // Given
        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                "테스트 검색어", "latest", 2, 20, "title"
        );

        // Then - Request 객체가 올바르게 생성되었는지 확인
        assertThat(request.query()).isEqualTo("테스트 검색어");
        assertThat(request.sort()).isEqualTo("latest");
        assertThat(request.page()).isEqualTo(2);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.target()).isEqualTo("title");
    }
}
