package org.yyubin.application.review.query;

public record GetUserReviewsQuery(
        Long userId,
        Long viewerId,
        Long cursor,
        int size
) {

    public GetUserReviewsQuery {
        if (size <= 0) {
            size = 20;
        }
    }
}
