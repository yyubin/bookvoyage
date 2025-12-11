package org.yyubin.application.review;

import org.yyubin.application.review.command.DeleteReviewCommand;
import org.yyubin.application.review.dto.ReviewResult;

public interface DeleteReviewUseCase {

    ReviewResult execute(DeleteReviewCommand command);
}
