package org.yyubin.domain.notification;

public record NotificationId(Long value) {
    public NotificationId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid notification id");
        }
    }
}
