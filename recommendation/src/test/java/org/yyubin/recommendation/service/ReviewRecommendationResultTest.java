package org.yyubin.recommendation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewRecommendationResult 테스트")
class ReviewRecommendationResultTest {

    @Nested
    @DisplayName("Builder 테스트")
    class BuilderTest {

        @Test
        @DisplayName("Builder로 모든 필드를 설정할 수 있다")
        void builder_AllFields() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // When
            ReviewRecommendationResult result = ReviewRecommendationResult.builder()
                    .reviewId(100L)
                    .bookId(200L)
                    .score(0.95)
                    .rank(1)
                    .source("NEO4J_SIMILAR")
                    .reason("Similar users liked this review")
                    .createdAt(createdAt)
                    .build();

            // Then
            assertThat(result.getReviewId()).isEqualTo(100L);
            assertThat(result.getBookId()).isEqualTo(200L);
            assertThat(result.getScore()).isEqualTo(0.95);
            assertThat(result.getRank()).isEqualTo(1);
            assertThat(result.getSource()).isEqualTo("NEO4J_SIMILAR");
            assertThat(result.getReason()).isEqualTo("Similar users liked this review");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Builder로 필수 필드만 설정할 수 있다")
        void builder_MinimalFields() {
            // When
            ReviewRecommendationResult result = ReviewRecommendationResult.builder()
                    .reviewId(1L)
                    .build();

            // Then
            assertThat(result.getReviewId()).isEqualTo(1L);
            assertThat(result.getBookId()).isNull();
            assertThat(result.getScore()).isNull();
            assertThat(result.getRank()).isNull();
            assertThat(result.getSource()).isNull();
            assertThat(result.getReason()).isNull();
            assertThat(result.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("NoArgsConstructor로 빈 객체를 생성할 수 있다")
        void noArgsConstructor() {
            // When
            ReviewRecommendationResult result = new ReviewRecommendationResult();

            // Then
            assertThat(result.getReviewId()).isNull();
            assertThat(result.getBookId()).isNull();
            assertThat(result.getScore()).isNull();
        }

        @Test
        @DisplayName("AllArgsConstructor로 모든 필드를 설정할 수 있다")
        void allArgsConstructor() {
            // Given
            LocalDateTime createdAt = LocalDateTime.now();

            // When
            ReviewRecommendationResult result = new ReviewRecommendationResult(
                    100L, 200L, 0.85, 3, "ES_MLT", "More like this", createdAt
            );

            // Then
            assertThat(result.getReviewId()).isEqualTo(100L);
            assertThat(result.getBookId()).isEqualTo(200L);
            assertThat(result.getScore()).isEqualTo(0.85);
            assertThat(result.getRank()).isEqualTo(3);
            assertThat(result.getSource()).isEqualTo("ES_MLT");
            assertThat(result.getReason()).isEqualTo("More like this");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("Setter 테스트")
    class SetterTest {

        @Test
        @DisplayName("Setter로 필드 값을 변경할 수 있다")
        void setter_ModifyFields() {
            // Given
            ReviewRecommendationResult result = new ReviewRecommendationResult();
            LocalDateTime createdAt = LocalDateTime.now();

            // When
            result.setReviewId(999L);
            result.setBookId(888L);
            result.setScore(0.92);
            result.setRank(5);
            result.setSource("POPULARITY");
            result.setReason("Popular review");
            result.setCreatedAt(createdAt);

            // Then
            assertThat(result.getReviewId()).isEqualTo(999L);
            assertThat(result.getBookId()).isEqualTo(888L);
            assertThat(result.getScore()).isEqualTo(0.92);
            assertThat(result.getRank()).isEqualTo(5);
            assertThat(result.getSource()).isEqualTo("POPULARITY");
            assertThat(result.getReason()).isEqualTo("Popular review");
            assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("equals/hashCode/toString 테스트")
    class EqualsHashCodeToStringTest {

        @Test
        @DisplayName("동일한 필드를 가진 객체는 equals가 true")
        void equals_SameFields() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
            ReviewRecommendationResult result1 = ReviewRecommendationResult.builder()
                    .reviewId(1L)
                    .bookId(100L)
                    .score(0.9)
                    .rank(1)
                    .source("NEO4J")
                    .reason("Test reason")
                    .createdAt(createdAt)
                    .build();

            ReviewRecommendationResult result2 = ReviewRecommendationResult.builder()
                    .reviewId(1L)
                    .bookId(100L)
                    .score(0.9)
                    .rank(1)
                    .source("NEO4J")
                    .reason("Test reason")
                    .createdAt(createdAt)
                    .build();

            // Then
            assertThat(result1).isEqualTo(result2);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 객체는 equals가 false")
        void equals_DifferentFields() {
            // Given
            ReviewRecommendationResult result1 = ReviewRecommendationResult.builder()
                    .reviewId(1L)
                    .bookId(100L)
                    .build();

            ReviewRecommendationResult result2 = ReviewRecommendationResult.builder()
                    .reviewId(2L)
                    .bookId(200L)
                    .build();

            // Then
            assertThat(result1).isNotEqualTo(result2);
        }

        @Test
        @DisplayName("toString은 필드 정보를 포함")
        void toString_ContainsFieldInfo() {
            // Given
            ReviewRecommendationResult result = ReviewRecommendationResult.builder()
                    .reviewId(123L)
                    .bookId(456L)
                    .score(0.88)
                    .rank(2)
                    .source("FRESHNESS")
                    .reason("New review")
                    .build();

            // When
            String resultString = result.toString();

            // Then
            assertThat(resultString).contains("123");
            assertThat(resultString).contains("456");
            assertThat(resultString).contains("0.88");
            assertThat(resultString).contains("2");
            assertThat(resultString).contains("FRESHNESS");
        }
    }

    @Nested
    @DisplayName("Score 범위 테스트")
    class ScoreRangeTest {

        @Test
        @DisplayName("score는 0.0에서 1.0 사이 값을 저장할 수 있다")
        void score_RangeValues() {
            // When
            ReviewRecommendationResult result1 = ReviewRecommendationResult.builder().score(0.0).build();
            ReviewRecommendationResult result2 = ReviewRecommendationResult.builder().score(0.5).build();
            ReviewRecommendationResult result3 = ReviewRecommendationResult.builder().score(1.0).build();

            // Then
            assertThat(result1.getScore()).isEqualTo(0.0);
            assertThat(result2.getScore()).isEqualTo(0.5);
            assertThat(result3.getScore()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("score는 null일 수 있다")
        void score_NullValue() {
            // When
            ReviewRecommendationResult result = ReviewRecommendationResult.builder()
                    .reviewId(1L)
                    .build();

            // Then
            assertThat(result.getScore()).isNull();
        }
    }

    @Nested
    @DisplayName("Source 값 테스트")
    class SourceValueTest {

        @Test
        @DisplayName("다양한 source 값을 저장할 수 있다")
        void source_VariousValues() {
            // When
            ReviewRecommendationResult neo4j = ReviewRecommendationResult.builder()
                    .source("NEO4J_SIMILAR")
                    .build();
            ReviewRecommendationResult esMlt = ReviewRecommendationResult.builder()
                    .source("ES_MLT")
                    .build();
            ReviewRecommendationResult popularity = ReviewRecommendationResult.builder()
                    .source("POPULARITY")
                    .build();

            // Then
            assertThat(neo4j.getSource()).isEqualTo("NEO4J_SIMILAR");
            assertThat(esMlt.getSource()).isEqualTo("ES_MLT");
            assertThat(popularity.getSource()).isEqualTo("POPULARITY");
        }
    }
}
