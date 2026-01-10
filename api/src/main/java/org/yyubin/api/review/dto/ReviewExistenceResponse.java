package org.yyubin.api.review.dto;

public record ReviewExistenceResponse(
    boolean hasReview
) {
    public static ReviewExistenceResponse of(boolean hasReview) {
        return new ReviewExistenceResponse(hasReview);
    }
}
