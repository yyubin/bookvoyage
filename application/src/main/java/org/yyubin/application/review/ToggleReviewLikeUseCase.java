package org.yyubin.application.review;

import org.yyubin.application.review.command.ToggleReviewLikeCommand;

public interface ToggleReviewLikeUseCase {

    ToggleResult execute(ToggleReviewLikeCommand command);

    record ToggleResult(boolean isLiked, long likeCount) {
    }
}
