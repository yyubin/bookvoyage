package org.yyubin.application.userbook;

import org.yyubin.application.userbook.command.EnsureCompletedUserBookCommand;

public interface EnsureCompletedUserBookUseCase {
    void execute(EnsureCompletedUserBookCommand command);
}
