package org.yyubin.application.review;

import org.yyubin.application.review.command.CreateCommentCommand;
import org.yyubin.application.review.dto.ReviewCommentResult;

public interface CreateCommentUseCase {

    ReviewCommentResult execute(CreateCommentCommand command);
}
