package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimilarityScore domain tests")
class SimilarityScoreTest {

    @Test
    @DisplayName("of rejects out of range values")
    void ofRejectsOutOfRange() {
        assertThatThrownBy(() -> SimilarityScore.of(-0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SimilarityScore.of(1.1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("zero and max factories")
    void zeroAndMaxFactories() {
        assertThat(SimilarityScore.zero().getValue()).isEqualTo(0.0);
        assertThat(SimilarityScore.max().getValue()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("high and low similarity checks")
    void highLowChecks() {
        assertThat(SimilarityScore.of(0.7).isHighSimilarity()).isTrue();
        assertThat(SimilarityScore.of(0.29).isLowSimilarity()).isTrue();
    }
}
