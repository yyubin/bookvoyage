package org.yyubin.api.feed.dto;

import java.util.List;
import org.yyubin.application.feed.dto.FeedPageResult;

public record FeedPageResponse(
        List<FeedItemResponse> items,
        Long nextCursor
) {

    public static FeedPageResponse from(FeedPageResult result) {
        return new FeedPageResponse(
                result.items().stream().map(FeedItemResponse::from).toList(),
                result.nextCursorEpochMillis()
        );
    }
}
