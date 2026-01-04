package org.yyubin.application.recommendation.port.out;

import java.util.Optional;

/**
 * 의미론적 캐시 Port
 * Infrastructure 계층에서 구현 (RedisSemanticCacheAdapter)
 *
 * 일반 캐시와 달리 의미론적으로 유사한 쿼리도 캐시 히트 가능
 */
public interface SemanticCachePort {

    /**
     * 캐시 조회 (의미론적 유사도 기반)
     *
     * @param query 질의
     * @param category 카테고리 (user_analysis, community_trend 등)
     * @return 캐시된 응답 (Optional)
     */
    Optional<String> get(String query, String category);

    /**
     * 캐시 저장
     *
     * @param query 질의
     * @param response 응답
     * @param category 카테고리
     */
    void put(String query, String response, String category);

    /**
     * 캐시 초기화 (인덱스 생성)
     */
    void initialize();
}
