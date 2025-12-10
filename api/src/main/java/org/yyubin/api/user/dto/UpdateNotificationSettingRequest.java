package org.yyubin.api.user.dto;

public record UpdateNotificationSettingRequest(
        Boolean likeComment,
        Boolean mention,
        Boolean followeeReview
) {
}
