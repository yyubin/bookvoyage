package org.yyubin.infrastructure.persistence.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.notification.Notification;
import org.yyubin.domain.notification.NotificationId;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.user.UserId;

@Entity
@Table(name = "notification")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "content_id")
    private Long contentId;

    @Column(length = 255)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Notification toDomain() {
        return Notification.of(
                new NotificationId(id),
                new UserId(recipientId),
                type,
                actorId,
                contentId,
                message,
                createdAt,
                isRead
        );
    }

    public static NotificationEntity fromDomain(Notification notification) {
        return NotificationEntity.builder()
                .id(notification.getId() != null ? notification.getId().value() : null)
                .recipientId(notification.getRecipientId().value())
                .actorId(notification.getActorId())
                .type(notification.getType())
                .contentId(notification.getContentId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
