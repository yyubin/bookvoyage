package org.yyubin.api.review.dto;

import java.util.List;
import org.yyubin.application.review.dto.PagedReviewResult;

public record ReviewPageResponse(
        List<ReviewResponse> reviews,
        Long nextCursor
) {
    public static ReviewPageResponse from(PagedReviewResult result) {
        List<ReviewResponse> responses = result.reviews().stream()
                .map(ReviewResponse::from)
                .toList();
        return new ReviewPageResponse(responses, result.nextCursor());
    }
}
