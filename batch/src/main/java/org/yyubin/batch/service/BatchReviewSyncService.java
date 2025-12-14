package org.yyubin.batch.service;

import org.yyubin.batch.sync.ReviewSyncDto;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;

/**
 * 배치 작업을 위한 리뷰 동기화 서비스
 * Infrastructure Repository 직접 접근을 캡슐화
 */
public interface BatchReviewSyncService {

    /**
     * ReviewEntity로부터 동기화용 DTO를 생성
     *
     * @param entity 리뷰 엔티티
     * @return 동기화용 DTO (좋아요, 북마크, 댓글 수 등 통계 포함)
     */
    ReviewSyncDto buildSyncData(ReviewEntity entity);
}
