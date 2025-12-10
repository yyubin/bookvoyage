package org.yyubin.application.review;

import org.yyubin.application.review.command.DeleteReactionCommand;

public interface DeleteReactionUseCase {

    void execute(DeleteReactionCommand command);
}
