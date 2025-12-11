package org.yyubin.application.bookmark;

import org.yyubin.application.bookmark.command.RemoveBookmarkCommand;

public interface RemoveBookmarkUseCase {
    void remove(RemoveBookmarkCommand command);
}
