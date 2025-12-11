package org.yyubin.application.review;

import org.yyubin.application.review.command.UpdateReviewCommand;
import org.yyubin.application.review.dto.ReviewResult;

public interface UpdateReviewUseCase {

    ReviewResult execute(UpdateReviewCommand command);
}
