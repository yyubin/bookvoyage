package org.yyubin.application.review.port;

import java.util.Optional;
import org.yyubin.domain.book.Book;

public interface LoadBookPort {
    Optional<Book> loadByIsbn(String isbn);

    Optional<Book> loadById(Long bookId);
}
