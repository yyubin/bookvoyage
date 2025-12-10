package org.yyubin.infrastructure.persistence.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.notification.port.NotificationRepository;
import org.yyubin.domain.notification.Notification;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    @Transactional
    public Notification save(Notification notification) {
        NotificationEntity saved = notificationJpaRepository.save(NotificationEntity.fromDomain(notification));
        return saved.toDomain();
    }
}
