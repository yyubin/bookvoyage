package org.yyubin.domain.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ActivityType enum tests")
class ActivityTypeTest {

    @Test
    @DisplayName("enum contains expected values")
    void enumValues() {
        assertThat(ActivityType.valueOf("REVIEW_CREATED")).isEqualTo(ActivityType.REVIEW_CREATED);
        assertThat(ActivityType.values()).contains(ActivityType.USER_FOLLOWED);
    }
}
