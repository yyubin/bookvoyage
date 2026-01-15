package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewHighlight record tests")
class ReviewHighlightTest {

    @Test
    @DisplayName("record stores ids")
    void storesIds() {
        ReviewHighlight highlight = new ReviewHighlight(ReviewId.of(1L), new HighlightId(2L));

        assertThat(highlight.reviewId()).isEqualTo(ReviewId.of(1L));
        assertThat(highlight.highlightId().value()).isEqualTo(2L);
    }
}
