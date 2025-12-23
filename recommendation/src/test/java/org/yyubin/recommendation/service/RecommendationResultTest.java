package org.yyubin.recommendation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecommendationResult 테스트")
class RecommendationResultTest {

    @Test
    @DisplayName("Builder로 RecommendationResult 생성")
    void build_Success() {
        // Given & When
        RecommendationResult result = RecommendationResult.builder()
                .bookId(1L)
                .score(0.95)
                .rank(1)
                .source("NEO4J_COLLABORATIVE")
                .reason("Similar users also liked this book")
                .build();

        // Then
        assertThat(result.getBookId()).isEqualTo(1L);
        assertThat(result.getScore()).isEqualTo(0.95);
        assertThat(result.getRank()).isEqualTo(1);
        assertThat(result.getSource()).isEqualTo("NEO4J_COLLABORATIVE");
        assertThat(result.getReason()).isEqualTo("Similar users also liked this book");
    }

    @Test
    @DisplayName("Setter로 값 변경")
    void setters_Work() {
        // Given
        RecommendationResult result = new RecommendationResult();

        // When
        result.setBookId(100L);
        result.setScore(0.88);
        result.setRank(5);
        result.setSource("ELASTICSEARCH_MLT");
        result.setReason("More like this");

        // Then
        assertThat(result.getBookId()).isEqualTo(100L);
        assertThat(result.getScore()).isEqualTo(0.88);
        assertThat(result.getRank()).isEqualTo(5);
        assertThat(result.getSource()).isEqualTo("ELASTICSEARCH_MLT");
        assertThat(result.getReason()).isEqualTo("More like this");
    }

    @Test
    @DisplayName("Equals and HashCode 테스트")
    void equalsAndHashCode() {
        // Given
        RecommendationResult result1 = RecommendationResult.builder()
                .bookId(1L)
                .score(0.9)
                .rank(1)
                .source("NEO4J_GENRE")
                .reason("Same genre")
                .build();

        RecommendationResult result2 = RecommendationResult.builder()
                .bookId(1L)
                .score(0.9)
                .rank(1)
                .source("NEO4J_GENRE")
                .reason("Same genre")
                .build();

        RecommendationResult result3 = RecommendationResult.builder()
                .bookId(2L)
                .score(0.9)
                .rank(1)
                .source("NEO4J_GENRE")
                .reason("Same genre")
                .build();

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("toString 테스트")
    void toString_ContainsAllFields() {
        // Given
        RecommendationResult result = RecommendationResult.builder()
                .bookId(42L)
                .score(0.75)
                .rank(3)
                .source("POPULARITY")
                .reason("Popular book")
                .build();

        // When
        String resultString = result.toString();

        // Then
        assertThat(resultString).contains("42");
        assertThat(resultString).contains("0.75");
        assertThat(resultString).contains("3");
        assertThat(resultString).contains("POPULARITY");
        assertThat(resultString).contains("Popular book");
    }

    @Test
    @DisplayName("All args constructor 테스트")
    void allArgsConstructor_Works() {
        // Given & When
        RecommendationResult result = new RecommendationResult(
                10L,
                0.85,
                2,
                "NEO4J_AUTHOR",
                "Same author"
        );

        // Then
        assertThat(result.getBookId()).isEqualTo(10L);
        assertThat(result.getScore()).isEqualTo(0.85);
        assertThat(result.getRank()).isEqualTo(2);
        assertThat(result.getSource()).isEqualTo("NEO4J_AUTHOR");
        assertThat(result.getReason()).isEqualTo("Same author");
    }
}
