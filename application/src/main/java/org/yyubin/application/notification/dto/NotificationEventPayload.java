package org.yyubin.application.notification.dto;

import org.yyubin.domain.notification.NotificationType;

public record NotificationEventPayload(
        Long recipientId,
        NotificationType type,
        Long actorId,
        Long contentId,
        String message
) {
}
