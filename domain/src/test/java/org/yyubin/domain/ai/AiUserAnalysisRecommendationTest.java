package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiUserAnalysisRecommendation record tests")
class AiUserAnalysisRecommendationTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        AiUserAnalysisRecommendation rec = AiUserAnalysisRecommendation.of(
                1L,
                2L,
                3L,
                "Book",
                "Author",
                "Reason",
                1
        );

        assertThat(rec.bookTitle()).isEqualTo("Book");
        assertThat(rec.rank()).isEqualTo(1);
    }
}
