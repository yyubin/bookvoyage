package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HighlightId domain tests")
class HighlightIdTest {

    @Test
    @DisplayName("invalid id rejected")
    void invalidIdRejected() {
        assertThatThrownBy(() -> new HighlightId(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HighlightId(0L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valid id accepted")
    void validIdAccepted() {
        HighlightId id = new HighlightId(10L);
        assertThat(id.value()).isEqualTo(10L);
    }
}
