package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Highlight domain tests")
class HighlightTest {

    @Test
    @DisplayName("create validates raw value and normalizer")
    void createValidatesRawValue() {
        HighlightNormalizer normalizer = raw -> raw.trim();

        assertThatThrownBy(() -> Highlight.create(" ", normalizer))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Highlight.create(null, normalizer))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Highlight.create("value", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("create applies normalizer")
    void createAppliesNormalizer() {
        Highlight highlight = Highlight.create("  text  ", String::trim);

        assertThat(highlight.getRawValue()).isEqualTo("  text  ");
        assertThat(highlight.getNormalizedValue()).isEqualTo("text");
        assertThat(highlight.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("withId returns new instance with id")
    void withIdReturnsNewInstance() {
        Highlight highlight = Highlight.create("text", String::trim);
        Highlight withId = highlight.withId(5L);

        assertThat(withId.getId().value()).isEqualTo(5L);
        assertThat(withId.getRawValue()).isEqualTo(highlight.getRawValue());
    }

    @Test
    @DisplayName("of validates required fields")
    void ofValidatesRequiredFields() {
        assertThatThrownBy(() -> Highlight.of(null, "raw", "norm", LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }
}
