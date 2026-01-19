package org.yyubin.recommendation.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.search.document.ReviewDocument;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchReviewAdapter 테스트")
class SearchReviewAdapterTest {

    @Mock
    private ReviewDocumentRepository reviewDocumentRepository;

    @InjectMocks
    private SearchReviewAdapter searchReviewAdapter;

    @Nested
    @DisplayName("saveAll 테스트")
    class SaveAllTest {

        @Test
        @DisplayName("여러 ReviewDocument를 저장하면 저장된 문서 리스트를 반환한다")
        void saveAll_MultipleDocuments_ReturnsSavedList() {
            // Given
            ReviewDocument doc1 = ReviewDocument.builder()
                    .id("1")
                    .reviewId(1L)
                    .userId(100L)
                    .bookId(200L)
                    .bookTitle("테스트 도서 1")
                    .title("리뷰 제목 1")
                    .content("리뷰 내용 1")
                    .summary("요약 1")
                    .rating(4.5f)
                    .genre("판타지")
                    .visibility("PUBLIC")
                    .likeCount(10)
                    .commentCount(5)
                    .bookmarkCount(3)
                    .viewCount(100L)
                    .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .build();

            ReviewDocument doc2 = ReviewDocument.builder()
                    .id("2")
                    .reviewId(2L)
                    .userId(101L)
                    .bookId(201L)
                    .bookTitle("테스트 도서 2")
                    .title("리뷰 제목 2")
                    .content("리뷰 내용 2")
                    .summary("요약 2")
                    .rating(4.0f)
                    .genre("로맨스")
                    .visibility("PUBLIC")
                    .likeCount(20)
                    .commentCount(10)
                    .bookmarkCount(5)
                    .viewCount(200L)
                    .createdAt(LocalDateTime.of(2024, 2, 1, 10, 0))
                    .build();

            List<ReviewDocument> documents = Arrays.asList(doc1, doc2);

            when(reviewDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<ReviewDocument> result = searchReviewAdapter.saveAll(documents);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo("1");
            assertThat(result.get(0).getTitle()).isEqualTo("리뷰 제목 1");
            assertThat(result.get(1).getId()).isEqualTo("2");
            assertThat(result.get(1).getTitle()).isEqualTo("리뷰 제목 2");
            verify(reviewDocumentRepository).saveAll(documents);
        }

        @Test
        @DisplayName("빈 리스트를 저장하면 빈 리스트를 반환한다")
        void saveAll_EmptyList_ReturnsEmptyList() {
            // Given
            List<ReviewDocument> emptyList = Collections.emptyList();
            when(reviewDocumentRepository.saveAll(emptyList)).thenReturn(emptyList);

            // When
            List<ReviewDocument> result = searchReviewAdapter.saveAll(emptyList);

            // Then
            assertThat(result).isEmpty();
            verify(reviewDocumentRepository).saveAll(emptyList);
        }

        @Test
        @DisplayName("단일 ReviewDocument를 저장하면 저장된 문서를 포함한 리스트를 반환한다")
        void saveAll_SingleDocument_ReturnsSingleElementList() {
            // Given
            ReviewDocument doc = ReviewDocument.builder()
                    .id("1")
                    .reviewId(1L)
                    .userId(100L)
                    .bookId(200L)
                    .bookTitle("단일 도서")
                    .title("단일 리뷰")
                    .content("단일 내용")
                    .rating(5.0f)
                    .visibility("PUBLIC")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<ReviewDocument> documents = Collections.singletonList(doc);
            when(reviewDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<ReviewDocument> result = searchReviewAdapter.saveAll(documents);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("단일 리뷰");
            assertThat(result.get(0).getBookTitle()).isEqualTo("단일 도서");
            verify(reviewDocumentRepository).saveAll(documents);
        }

        @Test
        @DisplayName("하이라이트가 포함된 문서를 저장할 수 있다")
        void saveAll_WithHighlights_SavesCorrectly() {
            // Given
            ReviewDocument doc = ReviewDocument.builder()
                    .id("1")
                    .reviewId(1L)
                    .userId(100L)
                    .bookId(200L)
                    .title("하이라이트 리뷰")
                    .content("리뷰 내용")
                    .highlights(List.of("인상적인 문장 1", "인상적인 문장 2"))
                    .highlightsNorm(List.of("norm1", "norm2"))
                    .keywords(List.of("판타지", "모험", "성장"))
                    .visibility("PUBLIC")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<ReviewDocument> documents = Collections.singletonList(doc);
            when(reviewDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<ReviewDocument> result = searchReviewAdapter.saveAll(documents);

            // Then
            assertThat(result.get(0).getHighlights()).hasSize(2);
            assertThat(result.get(0).getHighlights()).contains("인상적인 문장 1", "인상적인 문장 2");
            assertThat(result.get(0).getKeywords()).contains("판타지", "모험");
        }

        @Test
        @DisplayName("품질 점수가 포함된 문서를 저장할 수 있다")
        void saveAll_WithQualityScores_SavesCorrectly() {
            // Given
            ReviewDocument doc = ReviewDocument.builder()
                    .id("1")
                    .reviewId(1L)
                    .userId(100L)
                    .bookId(200L)
                    .title("품질 리뷰")
                    .content("상세한 리뷰 내용")
                    .dwellScore(0.85f)
                    .avgDwellMs(45000L)
                    .ctr(0.12f)
                    .reachRate(0.75f)
                    .visibility("PUBLIC")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<ReviewDocument> documents = Collections.singletonList(doc);
            when(reviewDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<ReviewDocument> result = searchReviewAdapter.saveAll(documents);

            // Then
            assertThat(result.get(0).getDwellScore()).isEqualTo(0.85f);
            assertThat(result.get(0).getAvgDwellMs()).isEqualTo(45000L);
            assertThat(result.get(0).getCtr()).isEqualTo(0.12f);
            assertThat(result.get(0).getReachRate()).isEqualTo(0.75f);
        }

        @Test
        @DisplayName("searchableText가 포함된 문서를 저장할 수 있다")
        void saveAll_WithSearchableText_SavesCorrectly() {
            // Given
            String searchableText = ReviewDocument.buildSearchableText("검색용 리뷰 내용");

            ReviewDocument doc = ReviewDocument.builder()
                    .id("1")
                    .reviewId(1L)
                    .userId(100L)
                    .bookId(200L)
                    .title("검색 테스트 리뷰")
                    .content("검색용 리뷰 내용")
                    .searchableText(searchableText)
                    .visibility("PUBLIC")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<ReviewDocument> documents = Collections.singletonList(doc);
            when(reviewDocumentRepository.saveAll(documents)).thenReturn(documents);

            // When
            List<ReviewDocument> result = searchReviewAdapter.saveAll(documents);

            // Then
            assertThat(result.get(0).getSearchableText()).isEqualTo("검색용 리뷰 내용");
        }
    }
}
