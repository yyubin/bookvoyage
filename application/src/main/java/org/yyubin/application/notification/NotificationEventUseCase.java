package org.yyubin.application.notification;

import org.yyubin.application.notification.dto.NotificationEventPayload;

public interface NotificationEventUseCase {
    void handle(NotificationEventPayload payload);
}
