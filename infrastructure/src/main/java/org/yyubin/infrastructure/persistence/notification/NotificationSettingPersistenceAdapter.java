package org.yyubin.infrastructure.persistence.notification;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingPersistenceAdapter implements NotificationSettingPort {

    private final NotificationSettingJpaRepository notificationSettingJpaRepository;

    @Override
    public Optional<NotificationSetting> load(UserId userId) {
        return notificationSettingJpaRepository.findByUserId(userId.value())
                .map(NotificationSettingEntity::toDomain);
    }

    @Override
    @Transactional
    public NotificationSetting save(NotificationSetting setting) {
        NotificationSettingEntity entity = NotificationSettingEntity.fromDomain(setting);
        return notificationSettingJpaRepository.save(entity).toDomain();
    }
}
