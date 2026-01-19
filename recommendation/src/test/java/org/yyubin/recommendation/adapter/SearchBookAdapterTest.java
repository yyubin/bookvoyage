package org.yyubin.recommendation.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchBookAdapter 테스트")
class SearchBookAdapterTest {

    @Mock
    private BookDocumentRepository bookDocumentRepository;

    @InjectMocks
    private SearchBookAdapter searchBookAdapter;

    @Nested
    @DisplayName("saveAll 테스트")
    class SaveAllTest {

        @Test
        @DisplayName("여러 BookDocument를 저장하면 저장된 문서 리스트를 반환한다")
        void saveAll_MultipleDocuments_ReturnsSavedList() {
            // Given
            BookDocument doc1 = BookDocument.builder()
                    .id("1")
                    .title("테스트 도서 1")
                    .description("설명 1")
                    .isbn("978-1234567890")
                    .authors(List.of("저자1"))
                    .genres(List.of("판타지"))
                    .viewCount(100)
                    .wishlistCount(50)
                    .reviewCount(10)
                    .averageRating(4.5f)
                    .publishedDate(LocalDate.of(2024, 1, 1))
                    .build();

            BookDocument doc2 = BookDocument.builder()
                    .id("2")
                    .title("테스트 도서 2")
                    .description("설명 2")
                    .isbn("978-0987654321")
                    .authors(List.of("저자2"))
                    .genres(List.of("로맨스"))
                    .viewCount(200)
                    .wishlistCount(100)
                    .reviewCount(20)
                    .averageRating(4.0f)
                    .publishedDate(LocalDate.of(2024, 2, 1))
                    .build();

            List<BookDocument> documents = Arrays.asList(doc1, doc2);

            when(bookDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<BookDocument> result = searchBookAdapter.saveAll(documents);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("1");
            assertThat(result.get(1).getId()).isEqualTo("2");
            verify(bookDocumentRepository).saveAll(documents);
        }

        @Test
        @DisplayName("빈 리스트를 저장하면 빈 리스트를 반환한다")
        void saveAll_EmptyList_ReturnsEmptyList() {
            // Given
            List<BookDocument> emptyList = Collections.emptyList();
            when(bookDocumentRepository.saveAll(emptyList)).thenReturn(emptyList);

            // When
            List<BookDocument> result = searchBookAdapter.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
            verify(bookDocumentRepository).saveAll(emptyList);
        }

        @Test
        @DisplayName("단일 BookDocument를 저장하면 저장된 문서를 포함한 리스트를 반환한다")
        void saveAll_SingleDocument_ReturnsSingleElementList() {
            // Given
            BookDocument doc = BookDocument.builder()
                    .id("1")
                    .title("단일 도서")
                    .description("단일 설명")
                    .isbn("978-1111111111")
                    .authors(List.of("단독 저자"))
                    .genres(List.of("SF"))
                    .topics(List.of("우주", "미래"))
                    .viewCount(50)
                    .wishlistCount(25)
                    .reviewCount(5)
                    .averageRating(4.8f)
                    .publishedDate(LocalDate.of(2024, 3, 1))
                    .build();

            List<BookDocument> documents = Collections.singletonList(doc);
            when(bookDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<BookDocument> result = searchBookAdapter.saveAll(documents);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("단일 도서");
            assertThat(result.get(0).getAuthors()).containsExactly("단독 저자");
            verify(bookDocumentRepository).saveAll(documents);
        }

        @Test
        @DisplayName("searchableText가 포함된 문서를 저장할 수 있다")
        void saveAll_WithSearchableText_SavesCorrectly() {
            // Given
            String searchableText = BookDocument.buildSearchableText(
                    "테스트 제목",
                    "테스트 설명",
                    List.of("저자1", "저자2")
            );

            BookDocument doc = BookDocument.builder()
                    .id("1")
                    .title("테스트 제목")
                    .description("테스트 설명")
                    .authors(List.of("저자1", "저자2"))
                    .searchableText(searchableText)
                    .build();

            List<BookDocument> documents = Collections.singletonList(doc);
            when(bookDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<BookDocument> result = searchBookAdapter.saveAll(documents);

            // Then
            assertThat(result.get(0).getSearchableText()).contains("테스트 제목");
            assertThat(result.get(0).getSearchableText()).contains("테스트 설명");
            assertThat(result.get(0).getSearchableText()).contains("저자1");
        }
    }
}
