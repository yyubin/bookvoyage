package org.yyubin.application.review.port;

import org.yyubin.domain.book.Book;

public interface SaveBookPort {
    Book save(Book book);
}
