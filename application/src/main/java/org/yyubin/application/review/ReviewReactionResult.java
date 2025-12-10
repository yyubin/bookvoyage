package org.yyubin.application.review;

import java.time.LocalDateTime;
import org.yyubin.domain.review.ReviewReaction;

public record ReviewReactionResult(
        Long reactionId,
        Long reviewId,
        Long userId,
        String content,
        LocalDateTime createdAt
) {

    public static ReviewReactionResult from(ReviewReaction reaction) {
        return new ReviewReactionResult(
                reaction.getId().getValue(),
                reaction.getReviewId().getValue(),
                reaction.getUserId().value(),
                reaction.getContent(),
                reaction.getCreatedAt()
        );
    }
}
