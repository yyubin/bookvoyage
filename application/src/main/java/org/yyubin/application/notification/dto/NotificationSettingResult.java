package org.yyubin.application.notification.dto;

import org.yyubin.domain.notification.NotificationSetting;

public record NotificationSettingResult(
        Long userId,
        boolean likeAndCommentEnabled,
        boolean mentionEnabled,
        boolean followeeReviewEnabled
) {

    public static NotificationSettingResult from(NotificationSetting setting) {
        return new NotificationSettingResult(
                setting.getUserId().value(),
                setting.isLikeAndCommentEnabled(),
                setting.isMentionEnabled(),
                setting.isFolloweeReviewEnabled()
        );
    }
}
