package org.yyubin.application.review;

import org.yyubin.application.review.command.DeleteReviewCommand;

public interface DeleteReviewUseCase {

    ReviewResult execute(DeleteReviewCommand command);
}
