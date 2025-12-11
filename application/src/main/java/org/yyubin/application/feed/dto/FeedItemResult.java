package org.yyubin.application.feed.dto;

import java.time.LocalDateTime;
import org.yyubin.application.review.dto.ReviewResult;

public record FeedItemResult(
        Long feedItemId,
        LocalDateTime createdAt,
        ReviewResult review
) {
}
