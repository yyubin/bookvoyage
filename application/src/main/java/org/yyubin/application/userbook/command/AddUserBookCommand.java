package org.yyubin.application.userbook.command;

import org.yyubin.domain.book.BookSearchItem;

public record AddUserBookCommand(
        Long userId,
        BookSearchItem bookSearchItem,
        String status
) {
}
