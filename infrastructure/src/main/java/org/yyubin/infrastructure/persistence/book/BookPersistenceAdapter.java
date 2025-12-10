package org.yyubin.infrastructure.persistence.book;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.domain.book.Book;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookPersistenceAdapter implements LoadBookPort, SaveBookPort {

    private final BookJpaRepository bookJpaRepository;

    @Override
    public Optional<Book> loadByIsbn(String isbn) {
        return bookJpaRepository.findByIsbn(isbn)
                .map(BookEntity::toDomain);
    }

    @Override
    public Optional<Book> loadById(Long bookId) {
        return bookJpaRepository.findById(bookId)
                .map(BookEntity::toDomain);
    }

    @Override
    @Transactional
    public Book save(Book book) {
        BookEntity entity = BookEntity.fromDomain(book);
        return bookJpaRepository.save(entity).toDomain();
    }
}
