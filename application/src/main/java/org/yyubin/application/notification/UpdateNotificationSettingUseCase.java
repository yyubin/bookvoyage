package org.yyubin.application.notification;

import org.yyubin.application.notification.command.UpdateNotificationSettingCommand;
import org.yyubin.application.notification.dto.NotificationSettingResult;

public interface UpdateNotificationSettingUseCase {
    NotificationSettingResult update(UpdateNotificationSettingCommand command);
}
