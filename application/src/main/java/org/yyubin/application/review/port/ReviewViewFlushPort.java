package org.yyubin.application.review.port;

import java.util.List;
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
     * 배치로 여러 리뷰의 조회수를 업데이트 (단일 쿼리로 처리)
     *
     * @param updates 업데이트할 리뷰 ID와 증분값 리스트
     */
    void batchUpdateViewCount(List<CounterUpdate> updates);

    /**
     * ES 조회수 업데이트 (부분 업데이트)
     */
    void updateSearchIndexViewCount(Map<Long, Long> deltas);

    /**
     * 조회수 업데이트를 위한 레코드
     *
     * @param reviewId 리뷰 ID
     * @param delta 증분값 (현재 조회수에 더할 값)
     */
    record CounterUpdate(Long reviewId, long delta) {}
}
