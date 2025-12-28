package org.yyubin.application.book.query;

public record GetBookReviewsQuery(
        Long bookId,
        Long cursor,
        int size,
        String sort  // "recommended", "latest", "popular"
) {
    public GetBookReviewsQuery {
        if (sort == null || sort.isBlank()) {
            sort = "recommended";
        }
    }
}
