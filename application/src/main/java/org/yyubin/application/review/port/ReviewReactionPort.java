package org.yyubin.application.review.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.yyubin.domain.review.ReviewReaction;

public interface ReviewReactionPort {

    Optional<ReviewReaction> loadByReviewIdAndUserId(Long reviewId, Long userId);

    ReviewReaction save(ReviewReaction reaction);

    void delete(Long reviewId, Long userId);

    /**
     * 리뷰의 리액션 타입별 집계 조회
     */
    List<ReactionCount> countByReviewIdGroupByContent(Long reviewId);

    /**
     * 여러 리뷰의 리액션 개수를 배치로 조회
     */
    Map<Long, Long> countByReviewIdsBatch(List<Long> reviewIds);

    /**
     * 리액션 타입별 개수
     */
    interface ReactionCount {
        String getEmoji();
        Long getCount();
    }
}
