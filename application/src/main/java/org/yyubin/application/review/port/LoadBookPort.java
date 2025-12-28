package org.yyubin.application.review.port;

import java.util.List;
import java.util.Optional;
import org.yyubin.domain.book.Book;

public interface LoadBookPort {
    Optional<Book> loadByIdentifiers(String isbn10, String isbn13, String googleVolumeId);

    Optional<Book> loadById(Long bookId);

    List<Book> findAll();
}
