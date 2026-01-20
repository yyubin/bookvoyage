package org.yyubin.infrastructure.external.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.exception.ExternalBookSearchException;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.domain.book.BookSearchItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompositeBookSearchAdapter 테스트")
class CompositeBookSearchAdapterTest {

    @Mock
    private KakaoBooksSearchAdapter kakaoAdapter;

    @Mock
    private GoogleBooksSearchAdapter googleAdapter;

    @InjectMocks
    private CompositeBookSearchAdapter compositeAdapter;

    @Test
    @DisplayName("카카오 결과가 있으면 카카오 결과를 반환한다")
    void search_KakaoHasResults_ReturnsKakaoResults() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        BookSearchItem kakaoItem = BookSearchItem.of(
                "카카오 책", List.of("저자"), "1234567890", null, null, null, null, null, null, null, null
        );
        ExternalBookSearchResult kakaoResult = new ExternalBookSearchResult(List.of(kakaoItem), 1);

        when(kakaoAdapter.search(query)).thenReturn(kakaoResult);

        // When
        ExternalBookSearchResult result = compositeAdapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("카카오 책");
        verify(googleAdapter, never()).search(any());
    }

    @Test
    @DisplayName("카카오 결과가 비어있으면 구글로 폴백한다")
    void search_KakaoEmpty_FallsBackToGoogle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        ExternalBookSearchResult emptyKakaoResult = new ExternalBookSearchResult(List.of(), 0);

        BookSearchItem googleItem = BookSearchItem.of(
                "구글 책", List.of("저자"), null, "9781234567890", null, null, null, null, null, null, "googleId"
        );
        ExternalBookSearchResult googleResult = new ExternalBookSearchResult(List.of(googleItem), 1);

        when(kakaoAdapter.search(query)).thenReturn(emptyKakaoResult);
        when(googleAdapter.search(query)).thenReturn(googleResult);

        // When
        ExternalBookSearchResult result = compositeAdapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("구글 책");
        verify(kakaoAdapter).search(query);
        verify(googleAdapter).search(query);
    }

    @Test
    @DisplayName("카카오 결과가 null이면 구글로 폴백한다")
    void search_KakaoItemsNull_FallsBackToGoogle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        ExternalBookSearchResult nullItemsResult = new ExternalBookSearchResult(null, 0);

        BookSearchItem googleItem = BookSearchItem.of(
                "구글 책", List.of("저자"), null, "9781234567890", null, null, null, null, null, null, "googleId"
        );
        ExternalBookSearchResult googleResult = new ExternalBookSearchResult(List.of(googleItem), 1);

        when(kakaoAdapter.search(query)).thenReturn(nullItemsResult);
        when(googleAdapter.search(query)).thenReturn(googleResult);

        // When
        ExternalBookSearchResult result = compositeAdapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("구글 책");
    }

    @Test
    @DisplayName("카카오 예외 발생 시 구글로 폴백한다")
    void search_KakaoThrowsException_FallsBackToGoogle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        BookSearchItem googleItem = BookSearchItem.of(
                "구글 책", List.of("저자"), null, "9781234567890", null, null, null, null, null, null, "googleId"
        );
        ExternalBookSearchResult googleResult = new ExternalBookSearchResult(List.of(googleItem), 1);

        when(kakaoAdapter.search(query)).thenThrow(new ExternalBookSearchException("Kakao API failed", null));
        when(googleAdapter.search(query)).thenReturn(googleResult);

        // When
        ExternalBookSearchResult result = compositeAdapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("구글 책");
    }

    @Test
    @DisplayName("카카오와 구글 모두 예외 발생 시 구글 예외를 던진다")
    void search_BothThrowException_ThrowsGoogleException() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        RuntimeException googleException = new ExternalBookSearchException("Google API failed", null);

        when(kakaoAdapter.search(query)).thenThrow(new ExternalBookSearchException("Kakao API failed", null));
        when(googleAdapter.search(query)).thenThrow(googleException);

        // When & Then
        assertThatThrownBy(() -> compositeAdapter.search(query))
                .isInstanceOf(ExternalBookSearchException.class)
                .hasMessageContaining("Google API failed");
    }

    @Test
    @DisplayName("카카오 결과가 있으면 구글은 호출하지 않는다")
    void search_KakaoHasResults_DoesNotCallGoogle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        BookSearchItem kakaoItem = BookSearchItem.of(
                "카카오 책", List.of("저자"), "1234567890", null, null, null, null, null, null, null, null
        );
        ExternalBookSearchResult kakaoResult = new ExternalBookSearchResult(List.of(kakaoItem), 1);

        when(kakaoAdapter.search(query)).thenReturn(kakaoResult);

        // When
        compositeAdapter.search(query);

        // Then
        verify(kakaoAdapter).search(query);
        verify(googleAdapter, never()).search(any());
    }

    @Test
    @DisplayName("구글도 빈 결과를 반환하면 빈 결과를 반환한다")
    void search_BothEmpty_ReturnsEmptyResult() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        ExternalBookSearchResult emptyResult = new ExternalBookSearchResult(List.of(), 0);

        when(kakaoAdapter.search(query)).thenReturn(emptyResult);
        when(googleAdapter.search(query)).thenReturn(emptyResult);

        // When
        ExternalBookSearchResult result = compositeAdapter.search(query);

        // Then
        assertThat(result.items()).isEmpty();
        assertThat(result.totalItems()).isZero();
    }

    @Test
    @DisplayName("카카오 RuntimeException 발생 시 구글로 폴백한다")
    void search_KakaoThrowsRuntimeException_FallsBackToGoogle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        BookSearchItem googleItem = BookSearchItem.of(
                "구글 책", List.of("저자"), null, "9781234567890", null, null, null, null, null, null, "googleId"
        );
        ExternalBookSearchResult googleResult = new ExternalBookSearchResult(List.of(googleItem), 1);

        when(kakaoAdapter.search(query)).thenThrow(new RuntimeException("Network error"));
        when(googleAdapter.search(query)).thenReturn(googleResult);

        // When
        ExternalBookSearchResult result = compositeAdapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("구글 책");
    }
}
