package org.yyubin.api.activity.dto;

import org.yyubin.api.common.TimeFormatter;
import org.yyubin.api.profile.dto.ReviewItemResponse;
import org.yyubin.application.activity.dto.ActivityFeedItemResult;

public record ActivityItemResponse(
        Long activityId,
        String type,
        String createdAt,
        ActivityActorResponse actor,
        ReviewItemResponse review
) {
    public static ActivityItemResponse from(ActivityFeedItemResult result) {
        ReviewItemResponse review =
                result.review() != null ? ReviewItemResponse.from(result.review()) : null;
        return new ActivityItemResponse(
                result.activityId(),
                result.type().name(),
                TimeFormatter.formatRelativeTime(result.createdAt()),
                ActivityActorResponse.from(result.actor()),
                review
        );
    }
}
