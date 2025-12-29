package org.yyubin.application.recommendation.query;

public record GetReviewRecommendationsQuery(
        Long userId,
        Long cursor,
        int limit,
        boolean forceRefresh
) {
    public GetReviewRecommendationsQuery {
        if (limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }
    }
}
