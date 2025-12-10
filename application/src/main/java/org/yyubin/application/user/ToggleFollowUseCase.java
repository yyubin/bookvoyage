package org.yyubin.application.user;

import org.yyubin.application.user.command.ToggleFollowCommand;
import org.yyubin.application.user.dto.ToggleFollowResult;

public interface ToggleFollowUseCase {

    ToggleFollowResult execute(ToggleFollowCommand command);
}
