package org.yyubin.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.notification.port.NotificationPublisher;
import org.yyubin.domain.notification.Notification;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.user.UserId;
import org.yyubin.application.notification.port.NotificationSettingPort;

@Service
@RequiredArgsConstructor
public class NotificationEventService implements NotificationEventUseCase {

    private final NotificationCreator notificationCreator;
    private final NotificationPublisher notificationPublisher;
    private final NotificationSettingPort notificationSettingPort;

    @Async
    @Override
    public void handle(NotificationEventPayload payload) {
        NotificationSetting setting = notificationSettingPort.load(new UserId(payload.recipientId()))
                .orElseGet(() -> NotificationSetting.defaultFor(new UserId(payload.recipientId())));

        if (!isEnabled(setting, payload.type())) {
            return;
        }

        Notification saved = notificationCreator.create(payload);
        notificationPublisher.publish(payload);
    }

    private boolean isEnabled(NotificationSetting setting, NotificationType type) {
        return switch (type) {
            case LIKE_ON_REVIEW, COMMENT_ON_REVIEW -> setting.isLikeAndCommentEnabled();
            case MENTION -> setting.isMentionEnabled();
            case FOLLOWEE_NEW_REVIEW -> setting.isFolloweeReviewEnabled();
        };
    }
}
