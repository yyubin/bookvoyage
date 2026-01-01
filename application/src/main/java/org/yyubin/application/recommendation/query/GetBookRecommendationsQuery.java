package org.yyubin.application.recommendation.query;

public record GetBookRecommendationsQuery(
        Long userId,
        Long cursor,
        int limit,
        boolean forceRefresh,
        boolean enableSampling,
        String sessionId
) {
    public GetBookRecommendationsQuery {
        if (limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }
    }

    /**
     * 기존 호환성 유지용 생성자
     */
    public GetBookRecommendationsQuery(Long userId, Long cursor, int limit, boolean forceRefresh) {
        this(userId, cursor, limit, forceRefresh, true, null);
    }
}
