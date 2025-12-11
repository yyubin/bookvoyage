package org.yyubin.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.notification.port.NotificationRepository;
import org.yyubin.domain.notification.Notification;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
public class NotificationCreator {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification create(NotificationEventPayload payload) {
        Notification notification = Notification.create(
                new UserId(payload.recipientId()),
                payload.type(),
                payload.actorId(),
                payload.contentId(),
                payload.message()
        );
        return notificationRepository.save(notification);
    }
}
