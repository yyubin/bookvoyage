package org.yyubin.application.review;

import org.yyubin.application.review.command.UpsertReactionCommand;

public interface UpsertReactionUseCase {

    ReviewReactionResult execute(UpsertReactionCommand command);
}
