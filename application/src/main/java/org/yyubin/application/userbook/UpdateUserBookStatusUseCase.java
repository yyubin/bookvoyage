package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.UpdateUserBookStatusCommand;
import org.yyubin.application.userbook.dto.UserBookResult;

public interface UpdateUserBookStatusUseCase {

    UserBookResult execute(UpdateUserBookStatusCommand command);
}
