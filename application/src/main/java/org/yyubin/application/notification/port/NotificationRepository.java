package org.yyubin.application.notification.port;

import org.yyubin.domain.notification.Notification;

public interface NotificationRepository {
    Notification save(Notification notification);
}
