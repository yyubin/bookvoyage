package org.yyubin.application.book.search.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.port.ExternalBookSearchPort;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;
import org.yyubin.domain.book.BookSearchItem;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookSearchService 테스트")
class BookSearchServiceTest {

    @Mock
    private ExternalBookSearchPort externalBookSearchPort;

    @InjectMocks
    private BookSearchService bookSearchService;

    @Test
    @DisplayName("책 검색 성공 - 기본값 적용")
    void query_SuccessWithDefaults() {
        // Given
        String keyword = "Java";
        SearchBooksQuery query = new SearchBooksQuery(keyword, null, null, null, null, null);

        List<BookSearchItem> items = Arrays.asList(
                BookSearchItem.of("Java Book 1", Arrays.asList("Author 1"), null, null, null, null, null, null, null, null, null),
                BookSearchItem.of("Java Book 2", Arrays.asList("Author 2"), null, null, null, null, null, null, null, null, null)
        );

        ExternalBookSearchResult externalResult = new ExternalBookSearchResult(items, 100);

        when(externalBookSearchPort.search(any(SearchBooksQuery.class))).thenReturn(externalResult);

        // When
        BookSearchPage result = bookSearchService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(2);
        assertThat(result.totalItems()).isEqualTo(100);
        assertThat(result.nextStartIndex()).isEqualTo(20); // 0 + 20(default size)

        verify(externalBookSearchPort).search(any(SearchBooksQuery.class));
    }

    @Test
    @DisplayName("책 검색 성공 - 커스텀 파라미터 적용")
    void query_SuccessWithCustomParams() {
        // Given
        String keyword = "Spring";
        SearchBooksQuery query = new SearchBooksQuery(
                keyword,
                10,
                5,
                "ko",
                SearchOrder.NEWEST,
                PrintType.BOOKS
        );

        List<BookSearchItem> items = Arrays.asList(
                BookSearchItem.of("Spring Book 1", Arrays.asList("Author 1"), null, null, null, null, null, null, null, null, null)
        );

        ExternalBookSearchResult externalResult = new ExternalBookSearchResult(items, 50);

        when(externalBookSearchPort.search(any(SearchBooksQuery.class))).thenReturn(externalResult);

        // When
        BookSearchPage result = bookSearchService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalItems()).isEqualTo(50);
        assertThat(result.nextStartIndex()).isEqualTo(15); // 10 + 5

        verify(externalBookSearchPort).search(any(SearchBooksQuery.class));
    }

    @Test
    @DisplayName("책 검색 성공 - 마지막 페이지인 경우 nextStartIndex는 null")
    void query_SuccessWithLastPage() {
        // Given
        String keyword = "Kotlin";
        SearchBooksQuery query = new SearchBooksQuery(keyword, 80, 20, null, null, null);

        List<BookSearchItem> items = Arrays.asList(
                BookSearchItem.of("Kotlin Book", Arrays.asList("Author"), null, null, null, null, null, null, null, null, null)
        );

        ExternalBookSearchResult externalResult = new ExternalBookSearchResult(items, 90);

        when(externalBookSearchPort.search(any(SearchBooksQuery.class))).thenReturn(externalResult);

        // When
        BookSearchPage result = bookSearchService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.totalItems()).isEqualTo(90);
        assertThat(result.nextStartIndex()).isNull(); // 100 >= 90, so no next page

        verify(externalBookSearchPort).search(any(SearchBooksQuery.class));
    }

    @Test
    @DisplayName("검색어가 null이면 예외 발생")
    void query_FailWithNullKeyword() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(null, null, null, null, null, null);

        // When & Then
        assertThatThrownBy(() -> bookSearchService.query(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("keyword must not be empty");
    }

    @Test
    @DisplayName("검색어가 빈 문자열이면 예외 발생")
    void query_FailWithEmptyKeyword() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery("   ", null, null, null, null, null);

        // When & Then
        assertThatThrownBy(() -> bookSearchService.query(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("keyword must not be empty");
    }

    @Test
    @DisplayName("query가 null이면 예외 발생")
    void query_FailWithNullQuery() {
        // When & Then
        assertThatThrownBy(() -> bookSearchService.query(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("query must not be null");
    }

    @Test
    @DisplayName("size가 최대값을 초과하면 40으로 제한됨")
    void query_SizeExceedsMaximum() {
        // Given
        String keyword = "Test";
        SearchBooksQuery query = new SearchBooksQuery(keyword, 0, 100, null, null, null);

        List<BookSearchItem> items = Arrays.asList(
                BookSearchItem.of("Test Book", Arrays.asList("Author"), null, null, null, null, null, null, null, null, null)
        );

        ExternalBookSearchResult externalResult = new ExternalBookSearchResult(items, 100);

        when(externalBookSearchPort.search(any(SearchBooksQuery.class))).thenReturn(externalResult);

        // When
        BookSearchPage result = bookSearchService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nextStartIndex()).isEqualTo(40); // 0 + 40(max size)

        verify(externalBookSearchPort).search(any(SearchBooksQuery.class));
    }

    @Test
    @DisplayName("음수 startIndex는 0으로 정규화됨")
    void query_NegativeStartIndexNormalized() {
        // Given
        String keyword = "Test";
        SearchBooksQuery query = new SearchBooksQuery(keyword, -10, 20, null, null, null);

        List<BookSearchItem> items = List.of();
        ExternalBookSearchResult externalResult = new ExternalBookSearchResult(items, 0);

        when(externalBookSearchPort.search(any(SearchBooksQuery.class))).thenReturn(externalResult);

        // When
        BookSearchPage result = bookSearchService.query(query);

        // Then
        assertThat(result).isNotNull();
        verify(externalBookSearchPort).search(any(SearchBooksQuery.class));
    }
}
