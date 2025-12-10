package org.yyubin.application.review;

import org.yyubin.application.review.command.DeleteCommentCommand;

public interface DeleteCommentUseCase {

    void execute(DeleteCommentCommand command);
}
