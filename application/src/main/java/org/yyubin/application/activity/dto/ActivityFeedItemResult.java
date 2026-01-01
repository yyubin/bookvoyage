package org.yyubin.application.activity.dto;

import java.time.LocalDateTime;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.domain.activity.ActivityType;

public record ActivityFeedItemResult(
        Long activityId,
        ActivityType type,
        LocalDateTime createdAt,
        ActivityActorResult actor,
        ReviewResult review
) {
}
