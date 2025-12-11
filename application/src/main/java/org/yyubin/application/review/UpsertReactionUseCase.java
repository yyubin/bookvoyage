package org.yyubin.application.review;

import org.yyubin.application.review.command.UpsertReactionCommand;
import org.yyubin.application.review.dto.ReviewReactionResult;

public interface UpsertReactionUseCase {

    ReviewReactionResult execute(UpsertReactionCommand command);
}
