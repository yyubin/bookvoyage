package org.yyubin.domain.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AiResultStatus enum tests")
class AiResultStatusTest {

    @Test
    @DisplayName("enum values include success and failed")
    void enumValues() {
        assertThat(AiResultStatus.valueOf("SUCCESS")).isEqualTo(AiResultStatus.SUCCESS);
        assertThat(AiResultStatus.values()).contains(AiResultStatus.FAILED);
    }
}
