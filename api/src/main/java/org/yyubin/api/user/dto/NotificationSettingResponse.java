package org.yyubin.api.user.dto;

import org.yyubin.application.notification.dto.NotificationSettingResult;

public record NotificationSettingResponse(
        boolean likeComment,
        boolean mention,
        boolean followeeReview
) {

    public static NotificationSettingResponse from(NotificationSettingResult result) {
        return new NotificationSettingResponse(
                result.likeAndCommentEnabled(),
                result.mentionEnabled(),
                result.followeeReviewEnabled()
        );
    }
}
