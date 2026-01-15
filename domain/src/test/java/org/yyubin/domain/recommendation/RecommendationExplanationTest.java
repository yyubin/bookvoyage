package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RecommendationExplanation record tests")
class RecommendationExplanationTest {

    @Test
    @DisplayName("getReasonFor returns empty when missing")
    void getReasonForReturnsEmptyWhenMissing() {
        RecommendationExplanation explanation = RecommendationExplanation.of(
                1L,
                2L,
                "because",
                Map.of("graph", "similar")
        );

        assertThat(explanation.getReasonFor("semantic")).isEmpty();
        assertThat(explanation.getReasonFor("graph")).isEqualTo("similar");
    }

    @Test
    @DisplayName("hasExplanation detects content")
    void hasExplanationDetectsContent() {
        RecommendationExplanation withText = RecommendationExplanation.of(1L, 2L, "text", Map.of());
        RecommendationExplanation blank = RecommendationExplanation.of(1L, 2L, " ", Map.of());

        assertThat(withText.hasExplanation()).isTrue();
        assertThat(blank.hasExplanation()).isFalse();
    }
}
