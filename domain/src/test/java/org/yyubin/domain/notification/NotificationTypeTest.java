package org.yyubin.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationType enum tests")
class NotificationTypeTest {

    @Test
    @DisplayName("enum values include mention")
    void enumValues() {
        assertThat(NotificationType.valueOf("MENTION")).isEqualTo(NotificationType.MENTION);
        assertThat(NotificationType.values()).contains(NotificationType.FOLLOWEE_NEW_REVIEW);
    }
}
