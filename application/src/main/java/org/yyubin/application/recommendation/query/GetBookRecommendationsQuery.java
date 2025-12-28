package org.yyubin.application.recommendation.query;

public record GetBookRecommendationsQuery(
        Long userId,
        int limit,
        boolean forceRefresh
) {
    public GetBookRecommendationsQuery {
        if (limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }
    }
}
