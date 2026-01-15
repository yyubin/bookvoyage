package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiPrompt record tests")
class AiPromptTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        LocalDateTime now = LocalDateTime.now();
        AiPrompt prompt = AiPrompt.of(1L, "trend", "desc", true, now, now);

        assertThat(prompt.id()).isEqualTo(1L);
        assertThat(prompt.promptKey()).isEqualTo("trend");
        assertThat(prompt.active()).isTrue();
    }
}
