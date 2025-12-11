package org.yyubin.application.review;

import org.yyubin.application.review.command.UpdateCommentCommand;
import org.yyubin.application.review.dto.ReviewCommentResult;

public interface UpdateCommentUseCase {

    ReviewCommentResult execute(UpdateCommentCommand command);
}
