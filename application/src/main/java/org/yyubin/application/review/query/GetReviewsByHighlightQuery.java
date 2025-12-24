package org.yyubin.application.review.query;

public record GetReviewsByHighlightQuery(
        String highlight,
        Long cursor,
        int size
) {
}
