package org.yyubin.application.feed.dto;

import java.util.Collections;
import java.util.List;

public record FeedPageResult(
        List<FeedItemResult> items,
        Long nextCursorEpochMillis
) {
    public FeedPageResult {
        items = items == null ? Collections.emptyList() : List.copyOf(items);
    }
}
