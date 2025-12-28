package org.yyubin.application.review.port;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewLike;
import org.yyubin.domain.user.UserId;

public interface ReviewLikePort {

    Optional<ReviewLike> findByReviewIdAndUserId(ReviewId reviewId, UserId userId);

    ReviewLike save(ReviewLike reviewLike);

    void delete(ReviewId reviewId, UserId userId);

    boolean exists(ReviewId reviewId, UserId userId);

    long countByReviewId(ReviewId reviewId);

    Map<Long, Long> countByReviewIdsBatch(List<Long> reviewIds);
}
