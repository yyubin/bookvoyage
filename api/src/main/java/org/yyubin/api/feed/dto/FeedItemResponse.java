package org.yyubin.api.feed.dto;

import java.time.LocalDateTime;
import org.yyubin.api.review.dto.ReviewResponse;
import org.yyubin.application.feed.dto.FeedItemResult;

public record FeedItemResponse(
        Long feedItemId,
        LocalDateTime createdAt,
        ReviewResponse review
) {

    public static FeedItemResponse from(FeedItemResult result) {
        return new FeedItemResponse(
                result.feedItemId(),
                result.createdAt(),
                ReviewResponse.from(result.review())
        );
    }
}
