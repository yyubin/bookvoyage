package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserAnalysis record tests")
class UserAnalysisTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        UserAnalysis.BookRecommendation recommendation = UserAnalysis.BookRecommendation.of("Book", "Author", "Reason");
        UserAnalysis analysis = UserAnalysis.of(1L, "persona", "summary", List.of("kw"), List.of(recommendation));

        assertThat(analysis.userId()).isEqualTo(1L);
        assertThat(analysis.recommendations()).hasSize(1);
    }
}
