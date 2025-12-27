package org.yyubin.api.feed.dto;

import java.time.LocalDateTime;
import org.yyubin.api.common.TimeFormatter;
import org.yyubin.api.review.dto.ReviewResponse;
import org.yyubin.application.feed.dto.FeedItemResult;

public record FeedItemResponse(
        Long feedItemId,
        String createdAt,
        ReviewResponse review
) {

    public static FeedItemResponse from(FeedItemResult result) {
        return new FeedItemResponse(
                result.feedItemId(),
                TimeFormatter.formatRelativeTime(result.createdAt()),
                ReviewResponse.from(result.review())
        );
    }
}
