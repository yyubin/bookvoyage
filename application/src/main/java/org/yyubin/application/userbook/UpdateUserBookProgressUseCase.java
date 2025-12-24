package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.UpdateUserBookProgressCommand;
import org.yyubin.application.userbook.dto.UserBookResult;

public interface UpdateUserBookProgressUseCase {

    UserBookResult execute(UpdateUserBookProgressCommand command);
}
