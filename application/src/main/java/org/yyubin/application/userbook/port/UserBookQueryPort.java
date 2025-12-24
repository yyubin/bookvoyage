package org.yyubin.application.userbook.port;

import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.ReadingStatus;

public interface UserBookQueryPort {

    long countByBookAndStatus(BookId bookId, ReadingStatus status);

    long countByUserAndStatus(UserId userId, ReadingStatus status);

    long countByUser(UserId userId);
}
