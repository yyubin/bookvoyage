package org.yyubin.domain.notification;

import java.time.LocalDateTime;
import java.util.Objects;
import org.yyubin.domain.user.UserId;

public class Notification {

    private final NotificationId id;
    private final UserId recipientId;
    private final NotificationType type;
    private final Long actorId;
    private final Long contentId;
    private final String message;
    private final LocalDateTime createdAt;
    private boolean isRead;

    private Notification(NotificationId id,
                         UserId recipientId,
                         NotificationType type,
                         Long actorId,
                         Long contentId,
                         String message,
                         LocalDateTime createdAt,
                         boolean isRead) {
        this.id = id;
        this.recipientId = Objects.requireNonNull(recipientId, "Recipient cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.actorId = actorId;
        this.contentId = contentId;
        this.message = message;
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.isRead = isRead;
    }

    public static Notification create(UserId recipientId,
                                      NotificationType type,
                                      Long actorId,
                                      Long contentId,
                                      String message) {
        return new Notification(
                null,
                recipientId,
                type,
                actorId,
                contentId,
                message,
                LocalDateTime.now(),
                false
        );
    }

    public static Notification of(NotificationId id,
                                  UserId recipientId,
                                  NotificationType type,
                                  Long actorId,
                                  Long contentId,
                                  String message,
                                  LocalDateTime createdAt,
                                  boolean isRead) {
        return new Notification(id, recipientId, type, actorId, contentId, message, createdAt, isRead);
    }

    public NotificationId getId() {
        return id;
    }

    public UserId getRecipientId() {
        return recipientId;
    }

    public NotificationType getType() {
        return type;
    }

    public Long getActorId() {
        return actorId;
    }

    public Long getContentId() {
        return contentId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
