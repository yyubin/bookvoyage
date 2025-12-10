package org.yyubin.application.notification;

import org.yyubin.application.notification.dto.NotificationSettingResult;
import org.yyubin.domain.user.UserId;

public interface GetNotificationSettingUseCase {
    NotificationSettingResult get(UserId userId);
}
