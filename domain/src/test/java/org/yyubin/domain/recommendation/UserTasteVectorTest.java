package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserTasteVectorTest {

    @Test
    void should_calculate_cosine_similarity_correctly() {
        // Given
        UserTasteVector vector1 = UserTasteVector.of(1L, Map.of(
            "keyword:SF", 0.8,
            "keyword:철학", 0.6,
            "genre:소설", 0.4
        ));

        UserTasteVector vector2 = UserTasteVector.of(2L, Map.of(
            "keyword:SF", 0.7,
            "keyword:철학", 0.5,
            "keyword:판타지", 0.3
        ));

        // When
        double similarity = vector1.cosineSimilarity(vector2);

        // Then
        assertThat(similarity).isGreaterThan(0.8).isLessThan(1.0);
    }

    @Test
    void should_return_1_for_self_similarity() {
        // Given
        UserTasteVector vector = UserTasteVector.of(1L, Map.of(
            "keyword:SF", 0.8,
            "keyword:철학", 0.6
        ));

        // When
        double similarity = vector.cosineSimilarity(vector);

        // Then
        assertThat(similarity).isEqualTo(1.0);
    }

    @Test
    void should_extract_top_keywords() {
        // Given
        UserTasteVector vector = UserTasteVector.of(1L, Map.of(
            "keyword:SF", 0.8,
            "keyword:철학", 0.6,
            "genre:소설", 0.4,
            "keyword:힐링", 0.2
        ));

        // When
        List<String> topKeywords = vector.getTopKeywords(2);

        // Then
        assertThat(topKeywords).hasSize(2);
        assertThat(topKeywords.get(0)).isEqualTo("keyword:SF");
        assertThat(topKeywords.get(1)).isEqualTo("keyword:철학");
    }
}
