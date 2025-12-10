package org.yyubin.application.notification.port;

import java.util.Optional;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.UserId;

public interface NotificationSettingPort {
    Optional<NotificationSetting> load(UserId userId);
    NotificationSetting save(NotificationSetting setting);
}
