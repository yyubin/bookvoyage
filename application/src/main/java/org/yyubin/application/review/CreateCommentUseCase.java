package org.yyubin.application.review;

import org.yyubin.application.review.command.CreateCommentCommand;

public interface CreateCommentUseCase {

    ReviewCommentResult execute(CreateCommentCommand command);
}
