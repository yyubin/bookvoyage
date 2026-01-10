package org.yyubin.application.review.query;

public record CheckUserReviewQuery(
    Long userId,
    Long bookId
) { }
