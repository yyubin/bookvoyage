package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimilarUser record tests")
class SimilarUserTest {

    @Test
    @DisplayName("invalid similarity throws")
    void invalidSimilarityThrows() {
        assertThatThrownBy(() -> new SimilarUser(1L, -0.1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SimilarUser(1L, 1.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("compareTo sorts descending")
    void compareToSortsDescending() {
        SimilarUser high = SimilarUser.of(1L, 0.9);
        SimilarUser low = SimilarUser.of(2L, 0.2);

        assertThat(high.compareTo(low)).isLessThan(0);
        assertThat(low.compareTo(high)).isGreaterThan(0);
    }
}
