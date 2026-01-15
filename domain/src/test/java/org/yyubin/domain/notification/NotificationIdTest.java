package org.yyubin.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationId record tests")
class NotificationIdTest {

    @Test
    @DisplayName("invalid id throws")
    void invalidIdThrows() {
        assertThatThrownBy(() -> new NotificationId(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NotificationId(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("valid id accepted")
    void validIdAccepted() {
        NotificationId id = new NotificationId(10L);
        assertThat(id.value()).isEqualTo(10L);
    }
}
