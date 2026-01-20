package org.yyubin.infrastructure.external.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksDocumentResponse;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksMetaResponse;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoBooksSearchAdapter 테스트")
class KakaoBooksSearchAdapterTest {

    @Mock
    private KakaoBooksClient client;

    private KakaoBooksProperties properties;
    private KakaoBooksSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new KakaoBooksProperties();
        properties.setMaxResults(50);
        adapter = new KakaoBooksSearchAdapter(client, properties);
    }

    @Test
    @DisplayName("검색 결과를 도메인 객체로 변환한다")
    void search_MapsResponseToDomain() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = new KakaoBooksDocumentResponse(
                "테스트 책",
                "내용 설명",
                "https://example.com",
                "1234567890 9781234567890",
                "2024-01-15T00:00:00.000+09:00",
                List.of("저자1", "저자2"),
                List.of("번역자"),
                "출판사",
                20000,
                18000,
                "https://thumbnail.url",
                "정상판매"
        );

        KakaoBooksMetaResponse meta = new KakaoBooksMetaResponse(100, 50, false);
        KakaoBooksSearchResponse response = new KakaoBooksSearchResponse(meta, List.of(document));

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
        assertThat(item.getCoverUrl()).isEqualTo("https://thumbnail.url");
        assertThat(item.getPublisher()).isEqualTo("출판사");
        assertThat(item.getDescription()).isEqualTo("내용 설명");
        assertThat(item.getLanguage()).isNull(); // 카카오 API는 언어 정보 미제공
        assertThat(item.getPageCount()).isNull(); // 카카오 API는 페이지 수 미제공
        assertThat(item.getGoogleVolumeId()).isNull();
    }

    @Test
    @DisplayName("startIndex를 페이지 번호로 변환한다")
    void search_ConvertStartIndexToPage() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 20, 10, null, null, null
        );

        when(client.search(any())).thenReturn(
                new KakaoBooksSearchResponse(new KakaoBooksMetaResponse(0, 0, true), List.of())
        );

        // When
        adapter.search(query);

        // Then
        ArgumentCaptor<KakaoBooksSearchRequest> captor = ArgumentCaptor.forClass(KakaoBooksSearchRequest.class);
        verify(client).search(captor.capture());

        // startIndex 20, size 10 -> page 3 (20/10 + 1)
        assertThat(captor.getValue().page()).isEqualTo(3);
    }

    @Test
    @DisplayName("size가 maxResults보다 크면 maxResults로 제한한다")
    void search_LimitsToMaxResults() {
        // Given
        properties.setMaxResults(50);
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 100, null, null, null
        );

        when(client.search(any())).thenReturn(
                new KakaoBooksSearchResponse(new KakaoBooksMetaResponse(0, 0, true), List.of())
        );

        // When
        adapter.search(query);

        // Then
        ArgumentCaptor<KakaoBooksSearchRequest> captor = ArgumentCaptor.forClass(KakaoBooksSearchRequest.class);
        verify(client).search(captor.capture());

        assertThat(captor.getValue().size()).isEqualTo(50);
    }

    @Test
    @DisplayName("ISBN 파싱 - ISBN10과 ISBN13이 둘 다 있는 경우")
    void parseIsbn_BothIsbn10AndIsbn13() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = createDocumentWithIsbn("1234567890 9781234567890");
        when(client.search(any())).thenReturn(createResponseWithDocument(document));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isEqualTo("1234567890");
        assertThat(result.items().get(0).getIsbn13()).isEqualTo("9781234567890");
    }

    @Test
    @DisplayName("ISBN 파싱 - ISBN13과 ISBN10 순서가 반대인 경우")
    void parseIsbn_ReversedOrder() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = createDocumentWithIsbn("9781234567890 1234567890");
        when(client.search(any())).thenReturn(createResponseWithDocument(document));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isEqualTo("1234567890");
        assertThat(result.items().get(0).getIsbn13()).isEqualTo("9781234567890");
    }

    @Test
    @DisplayName("ISBN 파싱 - ISBN13만 있는 경우")
    void parseIsbn_OnlyIsbn13() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = createDocumentWithIsbn("9781234567890");
        when(client.search(any())).thenReturn(createResponseWithDocument(document));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isNull();
        assertThat(result.items().get(0).getIsbn13()).isEqualTo("9781234567890");
    }

    @Test
    @DisplayName("ISBN 파싱 - ISBN10만 있는 경우")
    void parseIsbn_OnlyIsbn10() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = createDocumentWithIsbn("1234567890");
        when(client.search(any())).thenReturn(createResponseWithDocument(document));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isEqualTo("1234567890");
        assertThat(result.items().get(0).getIsbn13()).isNull();
    }

    @Test
    @DisplayName("ISBN 파싱 - 빈 문자열인 경우")
    void parseIsbn_EmptyString() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = createDocumentWithIsbn("");
        when(client.search(any())).thenReturn(createResponseWithDocument(document));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isNull();
        assertThat(result.items().get(0).getIsbn13()).isNull();
    }

    @Test
    @DisplayName("ISBN 파싱 - null인 경우")
    void parseIsbn_Null() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse document = createDocumentWithIsbn(null);
        when(client.search(any())).thenReturn(createResponseWithDocument(document));

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items().get(0).getIsbn10()).isNull();
        assertThat(result.items().get(0).getIsbn13()).isNull();
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
    @DisplayName("documents가 null이면 빈 리스트를 반환한다")
    void search_NullDocuments_ReturnsEmptyList() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        when(client.search(any())).thenReturn(
                new KakaoBooksSearchResponse(new KakaoBooksMetaResponse(0, 0, true), null)
        );

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("title이 없는 문서는 필터링된다")
    void search_FilterOutDocumentsWithoutTitle() {
        // Given
        SearchBooksQuery query = new SearchBooksQuery(
                "테스트", 0, 10, null, null, null
        );

        KakaoBooksDocumentResponse validDoc = createDocumentWithTitle("유효한 책");
        KakaoBooksDocumentResponse nullTitleDoc = createDocumentWithTitle(null);
        KakaoBooksDocumentResponse blankTitleDoc = createDocumentWithTitle("   ");

        when(client.search(any())).thenReturn(
                new KakaoBooksSearchResponse(
                        new KakaoBooksMetaResponse(3, 3, true),
                        List.of(validDoc, nullTitleDoc, blankTitleDoc)
                )
        );

        // When
        ExternalBookSearchResult result = adapter.search(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).getTitle()).isEqualTo("유효한 책");
    }

    private KakaoBooksDocumentResponse createDocumentWithIsbn(String isbn) {
        return new KakaoBooksDocumentResponse(
                "테스트 책", null, null, isbn, null, List.of(), List.of(), null, null, null, null, null
        );
    }

    private KakaoBooksDocumentResponse createDocumentWithTitle(String title) {
        return new KakaoBooksDocumentResponse(
                title, null, null, null, null, List.of(), List.of(), null, null, null, null, null
        );
    }

    private KakaoBooksSearchResponse createResponseWithDocument(KakaoBooksDocumentResponse document) {
        return new KakaoBooksSearchResponse(
                new KakaoBooksMetaResponse(1, 1, true),
                List.of(document)
        );
    }
}
