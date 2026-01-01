package org.yyubin.application.recommendation.port;

import java.util.List;

public interface BookRecommendationPort {

    /**
     * 추천 조회 (기존 메서드 - 하위 호환성 유지)
     */
    default List<RecommendationItem> getRecommendations(Long userId, Long cursor, int limit, boolean forceRefresh) {
        return getRecommendations(userId, cursor, limit, forceRefresh, false, null);
    }

    /**
     * 추천 조회 (샘플링 지원)
     *
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @param limit 조회 개수
     * @param forceRefresh 캐시 강제 갱신
     * @param enableSampling 윈도우 샘플링 활성화
     * @param sessionId 세션 ID (샘플링 일관성 보장)
     * @return 추천 아이템 리스트
     */
    List<RecommendationItem> getRecommendations(
            Long userId,
            Long cursor,
            int limit,
            boolean forceRefresh,
            boolean enableSampling,
            String sessionId
    );

    record RecommendationItem(
            Long bookId,
            Double score,
            Integer rank,
            String source,
            String reason
    ) {
    }
}
