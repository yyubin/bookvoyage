package org.yyubin.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification domain tests")
class NotificationTest {

    @Test
    @DisplayName("create sets default unread state")
    void createSetsDefaults() {
        Notification notification = Notification.create(
                new UserId(1L),
                NotificationType.MENTION,
                10L,
                20L,
                "message"
        );

        assertThat(notification.getId()).isNull();
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsRead flips state")
    void markAsReadFlipsState() {
        Notification notification = Notification.of(
                new NotificationId(1L),
                new UserId(2L),
                NotificationType.LIKE_ON_REVIEW,
                null,
                null,
                null,
                LocalDateTime.now(),
                false
        );

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("null recipient is rejected")
    void nullRecipientRejected() {
        assertThatThrownBy(() -> Notification.create(null, NotificationType.MENTION, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }
}
