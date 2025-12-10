package org.yyubin.api.review.dto;

import java.util.List;
import org.yyubin.application.review.PagedReviewResult;

public record UserReviewPageResponse(
        List<ReviewResponse> reviews,
        Long nextCursor
) {

    public static UserReviewPageResponse from(PagedReviewResult result) {
        List<ReviewResponse> responses = result.reviews().stream()
                .map(ReviewResponse::from)
                .toList();

        return new UserReviewPageResponse(responses, result.nextCursor());
    }
}
