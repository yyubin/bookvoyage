package org.yyubin.api.review.dto;

import java.time.LocalDateTime;
import org.yyubin.application.review.ReviewReactionResult;

public record ReactionResponse(
        Long reactionId,
        Long reviewId,
        Long userId,
        String content,
        LocalDateTime createdAt
) {

    public static ReactionResponse from(ReviewReactionResult result) {
        return new ReactionResponse(
                result.reactionId(),
                result.reviewId(),
                result.userId(),
                result.content(),
                result.createdAt()
        );
    }
}
