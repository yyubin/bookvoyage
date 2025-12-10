package org.yyubin.application.notification.port;

import org.yyubin.application.notification.dto.NotificationEventPayload;

public interface NotificationPublisher {
    void publish(NotificationEventPayload payload);
}
