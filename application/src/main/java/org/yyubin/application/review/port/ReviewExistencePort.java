package org.yyubin.application.review.port;

import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

public interface ReviewExistencePort {
    boolean existsByUserAndBook(UserId userId, BookId bookId);
}
