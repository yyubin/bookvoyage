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
    public Optional<Book> loadByIdentifiers(String isbn10, String isbn13, String googleVolumeId) {
        if (googleVolumeId != null && !googleVolumeId.isBlank()) {
            Optional<Book> byVolumeId = bookJpaRepository.findByGoogleVolumeId(googleVolumeId)
                    .map(BookEntity::toDomain);
            if (byVolumeId.isPresent()) {
                return byVolumeId;
            }
        }

        if (isbn13 != null && !isbn13.isBlank()) {
            Optional<Book> byIsbn13 = bookJpaRepository.findByIsbn13(isbn13)
                    .map(BookEntity::toDomain);
            if (byIsbn13.isPresent()) {
                return byIsbn13;
            }
        }

        if (isbn10 != null && !isbn10.isBlank()) {
            return bookJpaRepository.findByIsbn10(isbn10)
                    .map(BookEntity::toDomain);
        }

        return Optional.empty();
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
