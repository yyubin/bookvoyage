package org.yyubin.api.activity.dto;

import java.util.List;
import org.yyubin.application.activity.dto.ActivityFeedPageResult;

public record ActivityPageResponse(
        List<ActivityItemResponse> items,
        Long nextCursor
) {
    public static ActivityPageResponse from(ActivityFeedPageResult result) {
        return new ActivityPageResponse(
                result.items().stream().map(ActivityItemResponse::from).toList(),
                result.nextCursorEpochMillis()
        );
    }
}
