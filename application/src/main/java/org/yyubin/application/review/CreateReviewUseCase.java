package org.yyubin.application.review;

import org.yyubin.application.review.command.CreateReviewCommand;
import org.yyubin.application.review.dto.ReviewResult;

public interface CreateReviewUseCase {

    ReviewResult execute(CreateReviewCommand command);
}
