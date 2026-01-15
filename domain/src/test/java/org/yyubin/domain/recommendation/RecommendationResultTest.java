package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.book.BookId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RecommendationResult domain tests")
class RecommendationResultTest {

    @Test
    @DisplayName("null bookId rejected")
    void nullBookIdRejected() {
        assertThatThrownBy(() -> RecommendationResult.of(null, SimilarityScore.of(0.5), "reason"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("null score rejected")
    void nullScoreRejected() {
        assertThatThrownBy(() -> RecommendationResult.of(BookId.of(1L), null, "reason"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("null reason defaults to empty")
    void nullReasonDefaultsToEmpty() {
        RecommendationResult result = RecommendationResult.of(BookId.of(1L), SimilarityScore.of(0.8), null);

        assertThat(result.getReason()).isEmpty();
    }
}
