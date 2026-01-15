package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiUserAnalysisRecord record tests")
class AiUserAnalysisRecordTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        LocalDateTime now = LocalDateTime.now();
        AiUserAnalysisRecord record = AiUserAnalysisRecord.of(
                1L,
                2L,
                3L,
                "cache",
                "persona",
                "summary",
                List.of("kw1"),
                "{}",
                now,
                now.plusDays(1),
                AiResultStatus.SUCCESS,
                null,
                List.of(AiUserAnalysisRecommendation.of(1L, 1L, 10L, "Book", "Author", "Reason", 1))
        );

        assertThat(record.userId()).isEqualTo(2L);
        assertThat(record.recommendations()).hasSize(1);
    }
}
