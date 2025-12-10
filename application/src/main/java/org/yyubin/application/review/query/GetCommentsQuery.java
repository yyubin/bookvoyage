package org.yyubin.application.review.query;

public record GetCommentsQuery(
        Long reviewId,
        Long viewerId,
        Long cursor,
        int size
) {
    public GetCommentsQuery {
        if (size <= 0) {
            size = 20;
        }
    }
}
