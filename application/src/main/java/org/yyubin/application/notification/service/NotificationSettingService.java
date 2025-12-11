package org.yyubin.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.notification.GetNotificationSettingUseCase;
import org.yyubin.application.notification.UpdateNotificationSettingUseCase;
import org.yyubin.application.notification.command.UpdateNotificationSettingCommand;
import org.yyubin.application.notification.dto.NotificationSettingResult;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService implements GetNotificationSettingUseCase, UpdateNotificationSettingUseCase {

    private final NotificationSettingPort notificationSettingPort;

    @Override
    public NotificationSettingResult get(UserId userId) {
        NotificationSetting setting = notificationSettingPort.load(userId)
                .orElseGet(() -> notificationSettingPort.save(NotificationSetting.defaultFor(userId)));
        return NotificationSettingResult.from(setting);
    }

    @Override
    @Transactional
    public NotificationSettingResult update(UpdateNotificationSettingCommand command) {
        UserId userId = new UserId(command.userId());
        NotificationSetting setting = notificationSettingPort.load(userId)
                .orElseGet(() -> NotificationSetting.defaultFor(userId));

        if (command.likeAndCommentEnabled() != null) {
            if (command.likeAndCommentEnabled()) setting.enableLikeAndComment();
            else setting.disableLikeAndComment();
        }
        if (command.mentionEnabled() != null) {
            if (command.mentionEnabled()) setting.enableMention();
            else setting.disableMention();
        }
        if (command.followeeReviewEnabled() != null) {
            if (command.followeeReviewEnabled()) setting.enableFolloweeReview();
            else setting.disableFolloweeReview();
        }

        NotificationSetting saved = notificationSettingPort.save(setting);
        return NotificationSettingResult.from(saved);
    }
}
