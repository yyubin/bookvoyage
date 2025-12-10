package org.yyubin.application.review;

import org.yyubin.application.review.command.UpdateReviewCommand;

public interface UpdateReviewUseCase {

    ReviewResult execute(UpdateReviewCommand command);
}
