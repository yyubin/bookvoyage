package org.yyubin.application.review.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 리뷰 조회수/뷰 메트릭 포트
 */
public interface ReviewViewMetricPort {

    /**
     * 조회수 증가 (선택적으로 사용자 기반 중복 방지)
     *
     * @param reviewId 리뷰 ID
     * @param userId   사용자 ID (nullable)
     * @return 증가 이후 추정 조회수
     */
    long incrementAndGet(Long reviewId, Long userId);

    /**
     * 캐시된 조회수 조회
     */
    Optional<Long> getCachedCount(Long reviewId);

    /**
     * 여러 리뷰의 캐시된 조회수를 배치로 조회
     */
    Map<Long, Long> getBatchCachedCounts(List<Long> reviewIds);
}
