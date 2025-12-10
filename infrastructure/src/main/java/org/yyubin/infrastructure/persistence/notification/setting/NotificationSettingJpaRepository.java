package org.yyubin.infrastructure.persistence.notification.setting;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingJpaRepository extends JpaRepository<NotificationSettingEntity, Long> {
    Optional<NotificationSettingEntity> findByUserId(Long userId);
}
