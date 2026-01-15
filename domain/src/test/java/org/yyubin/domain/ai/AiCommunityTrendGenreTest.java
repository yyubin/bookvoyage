package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiCommunityTrendGenre record tests")
class AiCommunityTrendGenreTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        AiCommunityTrendGenre genre = AiCommunityTrendGenre.of("fantasy", 0.4, "up");

        assertThat(genre.genre()).isEqualTo("fantasy");
        assertThat(genre.percentage()).isEqualTo(0.4);
        assertThat(genre.mood()).isEqualTo("up");
    }
}
