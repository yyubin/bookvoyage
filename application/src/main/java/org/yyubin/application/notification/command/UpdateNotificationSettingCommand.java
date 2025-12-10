package org.yyubin.application.notification.command;

public record UpdateNotificationSettingCommand(
        Long userId,
        Boolean likeAndCommentEnabled,
        Boolean mentionEnabled,
        Boolean followeeReviewEnabled
) {
}
