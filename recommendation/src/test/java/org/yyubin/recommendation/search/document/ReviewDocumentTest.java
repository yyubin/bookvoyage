package org.yyubin.recommendation.search.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewDocument 테스트")
class ReviewDocumentTest {

    @Nested
    @DisplayName("ReviewDocument Builder")
    class ReviewDocumentBuilder {

        @Test
        @DisplayName("Builder로 모든 필드를 설정할 수 있다")
        void builder_AllFields() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
            List<String> highlights = List.of("highlight1", "highlight2");
            List<String> highlightsNorm = List.of("norm1", "norm2");
            List<String> keywords = List.of("keyword1", "keyword2");

            // When
            ReviewDocument document = ReviewDocument.builder()
                    .id("review-123")
                    .userId(1L)
                    .reviewId(123L)
                    .bookId(456L)
                    .bookTitle("Test Book Title")
                    .summary("Test summary")
                    .title("Review Title")
                    .content("This is a test review content")
                    .highlights(highlights)
                    .highlightsNorm(highlightsNorm)
                    .keywords(keywords)
                    .rating(4.5f)
                    .genre("FICTION")
                    .visibility("PUBLIC")
                    .createdAt(createdAt)
                    .likeCount(10)
                    .commentCount(5)
                    .bookmarkCount(3)
                    .viewCount(100L)
                    .dwellScore(0.8f)
                    .avgDwellMs(5000L)
                    .ctr(0.15f)
                    .reachRate(0.25f)
                    .searchableText("This is a test review content")
                    .build();

            // Then
            assertThat(document.getId()).isEqualTo("review-123");
            assertThat(document.getUserId()).isEqualTo(1L);
            assertThat(document.getReviewId()).isEqualTo(123L);
            assertThat(document.getBookId()).isEqualTo(456L);
            assertThat(document.getBookTitle()).isEqualTo("Test Book Title");
            assertThat(document.getSummary()).isEqualTo("Test summary");
            assertThat(document.getTitle()).isEqualTo("Review Title");
            assertThat(document.getContent()).isEqualTo("This is a test review content");
            assertThat(document.getHighlights()).containsExactly("highlight1", "highlight2");
            assertThat(document.getHighlightsNorm()).containsExactly("norm1", "norm2");
            assertThat(document.getKeywords()).containsExactly("keyword1", "keyword2");
            assertThat(document.getRating()).isEqualTo(4.5f);
            assertThat(document.getGenre()).isEqualTo("FICTION");
            assertThat(document.getVisibility()).isEqualTo("PUBLIC");
            assertThat(document.getCreatedAt()).isEqualTo(createdAt);
            assertThat(document.getLikeCount()).isEqualTo(10);
            assertThat(document.getCommentCount()).isEqualTo(5);
            assertThat(document.getBookmarkCount()).isEqualTo(3);
            assertThat(document.getViewCount()).isEqualTo(100L);
            assertThat(document.getDwellScore()).isEqualTo(0.8f);
            assertThat(document.getAvgDwellMs()).isEqualTo(5000L);
            assertThat(document.getCtr()).isEqualTo(0.15f);
            assertThat(document.getReachRate()).isEqualTo(0.25f);
            assertThat(document.getSearchableText()).isEqualTo("This is a test review content");
        }

        @Test
        @DisplayName("Builder로 필수 필드만 설정할 수 있다")
        void builder_MinimalFields() {
            // When
            ReviewDocument document = ReviewDocument.builder()
                    .id("1")
                    .reviewId(1L)
                    .userId(1L)
                    .bookId(1L)
                    .build();

            // Then
            assertThat(document.getId()).isEqualTo("1");
            assertThat(document.getReviewId()).isEqualTo(1L);
            assertThat(document.getUserId()).isEqualTo(1L);
            assertThat(document.getBookId()).isEqualTo(1L);
            assertThat(document.getContent()).isNull();
            assertThat(document.getRating()).isNull();
            assertThat(document.getLikeCount()).isNull();
            assertThat(document.getViewCount()).isNull();
        }

        @Test
        @DisplayName("NoArgsConstructor로 빈 객체를 생성할 수 있다")
        void noArgsConstructor() {
            // When
            ReviewDocument document = new ReviewDocument();

            // Then
            assertThat(document.getId()).isNull();
            assertThat(document.getUserId()).isNull();
            assertThat(document.getReviewId()).isNull();
            assertThat(document.getContent()).isNull();
        }

        @Test
        @DisplayName("AllArgsConstructor로 모든 필드를 설정할 수 있다")
        void allArgsConstructor() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now();
            List<String> highlights = List.of("h1");
            List<String> highlightsNorm = List.of("n1");
            List<String> keywords = List.of("k1");

            // When
            ReviewDocument document = new ReviewDocument(
                    "id-1", 1L, 100L, 200L, "Book Title",
                    "Summary", "Title", "Content",
                    highlights, highlightsNorm, keywords,
                    4.0f, "FICTION", "PUBLIC", createdAt,
                    10, 5, 3, 100L, 0.5f, 3000L, 0.1f, 0.2f, "searchable"
            );

            // Then
            assertThat(document.getId()).isEqualTo("id-1");
            assertThat(document.getUserId()).isEqualTo(1L);
            assertThat(document.getReviewId()).isEqualTo(100L);
            assertThat(document.getBookId()).isEqualTo(200L);
            assertThat(document.getBookTitle()).isEqualTo("Book Title");
            assertThat(document.getContent()).isEqualTo("Content");
        }
    }

    @Nested
    @DisplayName("ReviewDocument Setter")
    class ReviewDocumentSetter {

        @Test
        @DisplayName("Setter로 필드 값을 변경할 수 있다")
        void setter_ModifyFields() {
            // Given
            ReviewDocument document = new ReviewDocument();

            // When
            document.setId("updated-id");
            document.setUserId(999L);
            document.setReviewId(888L);
            document.setBookId(777L);
            document.setContent("Updated content");
            document.setRating(5.0f);
            document.setLikeCount(100);
            document.setViewCount(500L);

            // Then
            assertThat(document.getId()).isEqualTo("updated-id");
            assertThat(document.getUserId()).isEqualTo(999L);
            assertThat(document.getReviewId()).isEqualTo(888L);
            assertThat(document.getBookId()).isEqualTo(777L);
            assertThat(document.getContent()).isEqualTo("Updated content");
            assertThat(document.getRating()).isEqualTo(5.0f);
            assertThat(document.getLikeCount()).isEqualTo(100);
            assertThat(document.getViewCount()).isEqualTo(500L);
        }

        @Test
        @DisplayName("인기도 지표 필드들을 설정할 수 있다")
        void setter_PopularityMetrics() {
            // Given
            ReviewDocument document = new ReviewDocument();

            // When
            document.setDwellScore(0.75f);
            document.setAvgDwellMs(4500L);
            document.setCtr(0.12f);
            document.setReachRate(0.35f);

            // Then
            assertThat(document.getDwellScore()).isEqualTo(0.75f);
            assertThat(document.getAvgDwellMs()).isEqualTo(4500L);
            assertThat(document.getCtr()).isEqualTo(0.12f);
            assertThat(document.getReachRate()).isEqualTo(0.35f);
        }

        @Test
        @DisplayName("리스트 필드들을 설정할 수 있다")
        void setter_ListFields() {
            // Given
            ReviewDocument document = new ReviewDocument();
            List<String> highlights = List.of("highlight1", "highlight2");
            List<String> keywords = List.of("keyword1", "keyword2", "keyword3");

            // When
            document.setHighlights(highlights);
            document.setHighlightsNorm(List.of("norm1"));
            document.setKeywords(keywords);

            // Then
            assertThat(document.getHighlights()).hasSize(2);
            assertThat(document.getHighlightsNorm()).hasSize(1);
            assertThat(document.getKeywords()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("buildSearchableText 메서드")
    class BuildSearchableText {

        @Test
        @DisplayName("content가 있으면 그대로 반환")
        void buildSearchableText_WithContent() {
            // Given
            String content = "This is the review content for searching";

            // When
            String result = ReviewDocument.buildSearchableText(content);

            // Then
            assertThat(result).isEqualTo("This is the review content for searching");
        }

        @Test
        @DisplayName("content가 null이면 빈 문자열 반환")
        void buildSearchableText_NullContent() {
            // When
            String result = ReviewDocument.buildSearchableText(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("content가 빈 문자열이면 빈 문자열 반환")
        void buildSearchableText_EmptyContent() {
            // When
            String result = ReviewDocument.buildSearchableText("");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("content에 특수문자가 포함되어도 그대로 반환")
        void buildSearchableText_WithSpecialCharacters() {
            // Given
            String content = "Review with special chars: @#$%^&*() and unicode 한글!";

            // When
            String result = ReviewDocument.buildSearchableText(content);

            // Then
            assertThat(result).isEqualTo(content);
        }

        @Test
        @DisplayName("content에 줄바꿈이 포함되어도 그대로 반환")
        void buildSearchableText_WithNewlines() {
            // Given
            String content = "Line 1\nLine 2\nLine 3";

            // When
            String result = ReviewDocument.buildSearchableText(content);

            // Then
            assertThat(result).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("ReviewDocument equals/hashCode/toString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("동일한 필드를 가진 객체는 equals가 true")
        void equals_SameFields() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
            ReviewDocument doc1 = ReviewDocument.builder()
                    .id("1")
                    .reviewId(100L)
                    .userId(1L)
                    .bookId(10L)
                    .content("Same content")
                    .rating(4.0f)
                    .createdAt(createdAt)
                    .build();
            ReviewDocument doc2 = ReviewDocument.builder()
                    .id("1")
                    .reviewId(100L)
                    .userId(1L)
                    .bookId(10L)
                    .content("Same content")
                    .rating(4.0f)
                    .createdAt(createdAt)
                    .build();

            // Then
            assertThat(doc1).isEqualTo(doc2);
            assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 객체는 equals가 false")
        void equals_DifferentFields() {
            // Given
            ReviewDocument doc1 = ReviewDocument.builder()
                    .id("1")
                    .reviewId(100L)
                    .content("Content 1")
                    .build();
            ReviewDocument doc2 = ReviewDocument.builder()
                    .id("2")
                    .reviewId(200L)
                    .content("Content 2")
                    .build();

            // Then
            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("toString은 필드 정보를 포함")
        void toString_ContainsFieldInfo() {
            // Given
            ReviewDocument document = ReviewDocument.builder()
                    .id("review-123")
                    .reviewId(123L)
                    .userId(1L)
                    .bookId(456L)
                    .content("Test content")
                    .rating(4.5f)
                    .build();

            // When
            String result = document.toString();

            // Then
            assertThat(result).contains("ReviewDocument");
            assertThat(result).contains("id=review-123");
            assertThat(result).contains("reviewId=123");
            assertThat(result).contains("userId=1");
            assertThat(result).contains("bookId=456");
            assertThat(result).contains("rating=4.5");
        }
    }

    @Nested
    @DisplayName("ReviewDocument 인기도 지표")
    class PopularityMetrics {

        @Test
        @DisplayName("dwellScore는 0.0에서 1.0 사이 값을 저장할 수 있다")
        void dwellScore_RangeValues() {
            // When
            ReviewDocument doc1 = ReviewDocument.builder().dwellScore(0.0f).build();
            ReviewDocument doc2 = ReviewDocument.builder().dwellScore(0.5f).build();
            ReviewDocument doc3 = ReviewDocument.builder().dwellScore(1.0f).build();

            // Then
            assertThat(doc1.getDwellScore()).isEqualTo(0.0f);
            assertThat(doc2.getDwellScore()).isEqualTo(0.5f);
            assertThat(doc3.getDwellScore()).isEqualTo(1.0f);
        }

        @Test
        @DisplayName("ctr는 클릭률을 저장할 수 있다")
        void ctr_ClickThroughRate() {
            // When
            ReviewDocument document = ReviewDocument.builder()
                    .ctr(0.125f)
                    .build();

            // Then
            assertThat(document.getCtr()).isEqualTo(0.125f);
        }

        @Test
        @DisplayName("reachRate는 도달률을 저장할 수 있다")
        void reachRate_Values() {
            // When
            ReviewDocument document = ReviewDocument.builder()
                    .reachRate(0.75f)
                    .build();

            // Then
            assertThat(document.getReachRate()).isEqualTo(0.75f);
        }

        @Test
        @DisplayName("avgDwellMs는 평균 체류 시간을 저장할 수 있다")
        void avgDwellMs_Values() {
            // When
            ReviewDocument document = ReviewDocument.builder()
                    .avgDwellMs(15000L)
                    .build();

            // Then
            assertThat(document.getAvgDwellMs()).isEqualTo(15000L);
        }
    }

    @Nested
    @DisplayName("ReviewDocument visibility/genre")
    class VisibilityAndGenre {

        @Test
        @DisplayName("visibility 값을 설정할 수 있다")
        void visibility_Values() {
            // When
            ReviewDocument publicDoc = ReviewDocument.builder().visibility("PUBLIC").build();
            ReviewDocument privateDoc = ReviewDocument.builder().visibility("PRIVATE").build();
            ReviewDocument followersDoc = ReviewDocument.builder().visibility("FOLLOWERS_ONLY").build();

            // Then
            assertThat(publicDoc.getVisibility()).isEqualTo("PUBLIC");
            assertThat(privateDoc.getVisibility()).isEqualTo("PRIVATE");
            assertThat(followersDoc.getVisibility()).isEqualTo("FOLLOWERS_ONLY");
        }

        @Test
        @DisplayName("genre 값을 설정할 수 있다")
        void genre_Values() {
            // When
            ReviewDocument fictionDoc = ReviewDocument.builder().genre("FICTION").build();
            ReviewDocument nonFictionDoc = ReviewDocument.builder().genre("NON_FICTION").build();
            ReviewDocument sciFiDoc = ReviewDocument.builder().genre("SCI_FI").build();

            // Then
            assertThat(fictionDoc.getGenre()).isEqualTo("FICTION");
            assertThat(nonFictionDoc.getGenre()).isEqualTo("NON_FICTION");
            assertThat(sciFiDoc.getGenre()).isEqualTo("SCI_FI");
        }
    }
}
