package org.yyubin.application.activity.dto;

import java.util.Collections;
import java.util.List;

public record ActivityFeedPageResult(
        List<ActivityFeedItemResult> items,
        Long nextCursorEpochMillis
) {
    public ActivityFeedPageResult {
        items = items == null ? Collections.emptyList() : List.copyOf(items);
    }
}
