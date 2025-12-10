package org.yyubin.application.review.port;

import java.util.Optional;
import org.yyubin.domain.review.ReviewReaction;

public interface ReviewReactionPort {

    Optional<ReviewReaction> loadByReviewIdAndUserId(Long reviewId, Long userId);

    ReviewReaction save(ReviewReaction reaction);

    void delete(Long reviewId, Long userId);
}
