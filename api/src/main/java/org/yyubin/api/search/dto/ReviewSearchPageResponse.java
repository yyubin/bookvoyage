package org.yyubin.api.search.dto;

import java.util.List;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;

public record ReviewSearchPageResponse(
        List<ReviewSearchItemResponse> items,
        Long nextCursor
) {
    public static ReviewSearchPageResponse from(ReviewSearchPageResult result) {
        return new ReviewSearchPageResponse(
                result.items().stream().map(ReviewSearchItemResponse::from).toList(),
                result.nextCursor()
        );
    }
}
