package org.yyubin.infrastructure.external.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.infrastructure.external.book.dto.GoogleBookImageLinksResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBookIndustryIdentifierResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBookItemResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBookVolumeInfoResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksVolumeResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleBooksSearchAdapter 테스트")
class GoogleBooksSearchAdapterTest {

    @Mock
    private GoogleBooksClient client;

    private GoogleBooksProperties properties;
    private GoogleBooksSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new GoogleBooksProperties();
        properties.setMaxResults(40);
        adapter = new GoogleBooksSearchAdapter(client, properties);
    }

    @Test
    @DisplayName("검색 결과를 도메인 객체로 변환한다")
    void search_MapsResponseToDomain() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, "ko", null, null
        );

        GoogleBookVolumeInfoResponse volumeInfo = new GoogleBookVolumeInfoResponse(
                "테스트 책",
                List.of("저자1", "저자2"),
                "출판사",
                "2024-01-15",
                "설명입니다",
                300,
                "ko",
                new GoogleBookImageLinksResponse("smallUrl", "thumbnailUrl"),
                List.of(
                        new GoogleBookIndustryIdentifierResponse("ISBN_10", "1234567890"),
                        new GoogleBookIndustryIdentifierResponse("ISBN_13", "9781234567890")
                )
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(
                100,
                List.of(new GoogleBookItemResponse("volumeId1", volumeInfo))
        );

        when(client.search(any())).thenReturn(response);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.totalItems()).isEqualTo(100);
        assertThat(result.items()).hasSize(1);

        BookSearchItem item = result.items().get(0);
        assertThat(item.getTitle()).isEqualTo("테스트 책");
        assertThat(item.getAuthors()).containsExactly("저자1", "저자2");
        assertThat(item.getIsbn10()).isEqualTo("1234567890");
        assertThat(item.getIsbn13()).isEqualTo("9781234567890");
        assertThat(item.getCoverUrl()).isEqualTo("thumbnailUrl");
        assertThat(item.getPublisher()).isEqualTo("출판사");
        assertThat(item.getPublishedDate()).isEqualTo("2024-01-15");
        assertThat(item.getDescription()).isEqualTo("설명입니다");
        assertThat(item.getLanguage()).isEqualTo("ko");
        assertThat(item.getPageCount()).isEqualTo(300);
        assertThat(item.getGoogleVolumeId()).isEqualTo("volumeId1");
    }

    @Test
    @DisplayName("size가 maxResults보다 크면 maxResults로 제한한다")
    void search_LimitsToMaxResults() {
        // Given
        properties.setMaxResults(40);
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 100, null, null, null
        );

        when(client.search(any())).thenReturn(new GoogleBooksVolumeResponse(0, List.of()));

        // When
        adapter.search(query);

        // Then
        ArgumentCaptor<GoogleBooksSearchRequest> captor = ArgumentCaptor.forClass(GoogleBooksSearchRequest.class);
        verify(client).search(captor.capture());

        assertThat(captor.getValue().maxResults()).isEqualTo(40);
    }

    @Test
    @DisplayName("orderBy와 printType을 요청에 전달한다")
    void search_PassesOrderByAndPrintType() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, "ko", SearchOrder.NEWEST, PrintType.BOOKS
        );

        when(client.search(any())).thenReturn(new GoogleBooksVolumeResponse(0, List.of()));

        // When
        adapter.search(query);

        // Then
        ArgumentCaptor<GoogleBooksSearchRequest> captor = ArgumentCaptor.forClass(GoogleBooksSearchRequest.class);
        verify(client).search(captor.capture());

        assertThat(captor.getValue().orderBy()).isEqualTo("newest");
        assertThat(captor.getValue().printType()).isEqualTo("books");
    }

    @Test
    @DisplayName("응답이 null이면 빈 리스트를 반환한다")
    void search_NullResponse_ReturnsEmptyList() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        when(client.search(any())).thenReturn(null);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items()).isEmpty();
        assertThat(result.totalItems()).isZero();
    }

    @Test
    @DisplayName("items가 null이면 빈 리스트를 반환한다")
    void search_NullItems_ReturnsEmptyList() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        when(client.search(any())).thenReturn(new GoogleBooksVolumeResponse(0, null));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("title이 없는 항목은 필터링된다")
    void search_FilterOutItemsWithoutTitle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        GoogleBookVolumeInfoResponse validInfo = new GoogleBookVolumeInfoResponse(
                "유효한 책", List.of(), null, null, null, null, null, null, null
        );
        GoogleBookVolumeInfoResponse nullTitleInfo = new GoogleBookVolumeInfoResponse(
                null, List.of(), null, null, null, null, null, null, null
        );
        GoogleBookVolumeInfoResponse blankTitleInfo = new GoogleBookVolumeInfoResponse(
                "  ", List.of(), null, null, null, null, null, null, null
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(
                3,
                List.of(
                        new GoogleBookItemResponse("id1", validInfo),
                        new GoogleBookItemResponse("id2", nullTitleInfo),
                        new GoogleBookItemResponse("id3", blankTitleInfo)
                )
        );

        when(client.search(any())).thenReturn(response);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("유효한 책");
    }

    @Test
    @DisplayName("volumeInfo가 null인 항목은 필터링된다")
    void search_FilterOutItemsWithNullVolumeInfo() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(
                1,
                List.of(new GoogleBookItemResponse("id1", null))
        );

        when(client.search(any())).thenReturn(response);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("이미지가 없으면 coverUrl은 null이다")
    void search_NoImages_CoverUrlIsNull() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        GoogleBookVolumeInfoResponse volumeInfo = new GoogleBookVolumeInfoResponse(
                "테스트 책", List.of(), null, null, null, null, null, null, null
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(
                1,
                List.of(new GoogleBookItemResponse("id1", volumeInfo))
        );

        when(client.search(any())).thenReturn(response);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getCoverUrl()).isNull();
    }

    @Test
    @DisplayName("thumbnail이 없으면 smallThumbnail을 사용한다")
    void search_NoThumbnail_UsesSmallThumbnail() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        GoogleBookVolumeInfoResponse volumeInfo = new GoogleBookVolumeInfoResponse(
                "테스트 책", List.of(), null, null, null, null, null,
                new GoogleBookImageLinksResponse("smallUrl", null),
                null
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(
                1,
                List.of(new GoogleBookItemResponse("id1", volumeInfo))
        );

        when(client.search(any())).thenReturn(response);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getCoverUrl()).isEqualTo("smallUrl");
    }

    @Test
    @DisplayName("ISBN 식별자가 없으면 null을 반환한다")
    void search_NoIsbn_ReturnsNull() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        GoogleBookVolumeInfoResponse volumeInfo = new GoogleBookVolumeInfoResponse(
                "테스트 책", List.of(), null, null, null, null, null, null, null
        );

        GoogleBooksVolumeResponse response = new GoogleBooksVolumeResponse(
                1,
                List.of(new GoogleBookItemResponse("id1", volumeInfo))
        );

        when(client.search(any())).thenReturn(response);

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isNull();
        assertThat(result.items().get(0).getIsbn13()).isNull();
    }
}
