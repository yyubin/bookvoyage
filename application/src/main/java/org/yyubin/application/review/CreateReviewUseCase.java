package org.yyubin.application.review;

import org.yyubin.application.review.command.CreateReviewCommand;

public interface CreateReviewUseCase {

    ReviewResult execute(CreateReviewCommand command);
}
