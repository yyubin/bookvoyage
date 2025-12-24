package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.UpdateUserBookMemoCommand;
import org.yyubin.application.userbook.dto.UserBookResult;

public interface UpdateUserBookMemoUseCase {

    UserBookResult execute(UpdateUserBookMemoCommand command);
}
