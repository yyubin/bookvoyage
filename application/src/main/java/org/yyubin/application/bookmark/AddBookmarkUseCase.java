package org.yyubin.application.bookmark;

import org.yyubin.application.bookmark.command.AddBookmarkCommand;
import org.yyubin.domain.bookmark.ReviewBookmark;

public interface AddBookmarkUseCase {
    ReviewBookmark add(AddBookmarkCommand command);
}
