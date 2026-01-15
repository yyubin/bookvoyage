package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiCommunityTrendRecord record tests")
class AiCommunityTrendRecordTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        LocalDateTime now = LocalDateTime.now();
        AiCommunityTrendRecord record = AiCommunityTrendRecord.of(
                1L,
                2L,
                now.minusDays(1),
                now,
                List.of("keyword"),
                "summary",
                List.of(AiCommunityTrendGenre.of("fantasy", 0.5, "up")),
                "{}",
                now,
                now.plusDays(1),
                AiResultStatus.SUCCESS,
                null
        );

        assertThat(record.id()).isEqualTo(1L);
        assertThat(record.status()).isEqualTo(AiResultStatus.SUCCESS);
        assertThat(record.keywords()).contains("keyword");
    }
}
