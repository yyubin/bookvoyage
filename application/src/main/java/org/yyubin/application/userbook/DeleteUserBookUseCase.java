package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.DeleteUserBookCommand;

public interface DeleteUserBookUseCase {

    void execute(DeleteUserBookCommand command);
}
