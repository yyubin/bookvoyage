package org.yyubin.application.review.search.query;

public record SearchReviewsQuery(
        String keyword,
        Long cursor,
        Integer size
) {
    public SearchReviewsQuery {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("keyword must not be blank");
        }
    }
}
