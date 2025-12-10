package org.yyubin.application.review;

import org.yyubin.application.review.command.UpdateCommentCommand;

public interface UpdateCommentUseCase {

    ReviewCommentResult execute(UpdateCommentCommand command);
}
