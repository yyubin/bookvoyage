package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiRecommendationExplanationRecord record tests")
class AiRecommendationExplanationRecordTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        LocalDateTime now = LocalDateTime.now();
        AiRecommendationExplanationRecord record = AiRecommendationExplanationRecord.of(
                1L,
                2L,
                3L,
                4L,
                "explain",
                Map.of("graph", "similar"),
                "{}",
                now,
                now.plusDays(1),
                AiResultStatus.SUCCESS,
                null
        );

        assertThat(record.bookId()).isEqualTo(3L);
        assertThat(record.scoreDetails()).containsEntry("graph", "similar");
    }
}
