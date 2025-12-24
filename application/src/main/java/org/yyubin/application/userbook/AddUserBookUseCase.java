package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.AddUserBookCommand;
import org.yyubin.application.userbook.dto.UserBookResult;

public interface AddUserBookUseCase {

    UserBookResult execute(AddUserBookCommand command);
}
