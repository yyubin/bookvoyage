package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiPromptVersion record tests")
class AiPromptVersionTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        LocalDateTime now = LocalDateTime.now();
        AiPromptVersion version = AiPromptVersion.of(
                1L,
                2L,
                3,
                "template",
                "{\"in\":true}",
                "{\"out\":true}",
                "gpt",
                0.7,
                1200,
                "openai",
                true,
                "tester",
                now
        );

        assertThat(version.promptId()).isEqualTo(2L);
        assertThat(version.active()).isTrue();
        assertThat(version.temperature()).isEqualTo(0.7);
    }
}
