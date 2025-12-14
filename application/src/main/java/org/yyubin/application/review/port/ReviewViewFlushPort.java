package org.yyubin.application.review.port;

import java.util.Map;
import java.util.Optional;

/**
 * 조회수 플러시용 포트 (DB/ES 등)
 * Infrastructure 계층에서 구현
 */
public interface ReviewViewFlushPort {

    /**
     * 현재 DB 조회수 조회
     */
    Optional<Long> findCurrentViewCount(Long reviewId);

    /**
     * DB 조회수 업데이트
     */
    void updateViewCount(Long reviewId, long newCount);

    /**
     * ES 조회수 업데이트 (부분 업데이트)
     */
    void updateSearchIndexViewCount(Map<Long, Long> deltas);
}
