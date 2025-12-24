package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.UpdateUserBookRatingCommand;
import org.yyubin.application.userbook.dto.UserBookResult;

public interface UpdateUserBookRatingUseCase {

    UserBookResult execute(UpdateUserBookRatingCommand command);
}
