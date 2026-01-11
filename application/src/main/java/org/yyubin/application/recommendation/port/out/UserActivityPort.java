package org.yyubin.application.recommendation.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사용자 활동 조회 포트
 */
public interface UserActivityPort {

    /**
     * 사용자 활동 정보
     */
    record UserActivity(
        Long userId,
        List<ReviewActivity> bookmarkedReviews,
        List<ReviewActivity> likedReviews
    ) {}

    /**
     * 리뷰 활동 정보
     */
    record ReviewActivity(
        Long reviewId,
        Long bookId,
        String genre,
        List<String> keywords,
        LocalDateTime activityAt
    ) {}

    /**
     * 사용자의 활동 조회 (북마크, 좋아요)
     */
    UserActivity getUserActivity(Long userId);

    /**
     * 여러 사용자의 최근 리뷰 조회
     */
    List<ReviewWithKeywords> getRecentReviews(List<Long> userIds, LocalDateTime since);

    /**
     * 키워드를 포함한 리뷰 정보
     */
    record ReviewWithKeywords(
        Long reviewId,
        Long userId,
        Long bookId,
        String genre,
        List<String> keywords,
        long likeCount,
        LocalDateTime createdAt
    ) {}
}
