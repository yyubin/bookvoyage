package org.yyubin.infrastructure.persistence.userbook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.userbook.port.UserBookPort;
import org.yyubin.application.userbook.port.UserBookQueryPort;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.domain.userbook.UserBook;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookPersistenceAdapter implements UserBookPort, UserBookQueryPort {

    private final UserBookJpaRepository userBookJpaRepository;

    @Override
    public Optional<UserBook> findByUserAndBook(UserId userId, BookId bookId) {
        return userBookJpaRepository.findByUserIdAndBookIdAndDeletedFalse(
                userId.value(),
                bookId.getValue()
        ).map(UserBookEntity::toDomain);
    }

    @Override
    public boolean exists(UserId userId, BookId bookId) {
        return userBookJpaRepository.existsByUserIdAndBookIdAndDeletedFalse(
                userId.value(),
                bookId.getValue()
        );
    }

    @Override
    @Transactional
    public UserBook save(UserBook userBook) {
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);
        UserBookEntity saved = userBookJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void delete(UserId userId, BookId bookId) {
        userBookJpaRepository.softDeleteByUserIdAndBookId(
                userId.value(),
                bookId.getValue()
        );
    }

    @Override
    public List<UserBook> findByUser(UserId userId) {
        return userBookJpaRepository.findByUserIdAndDeletedFalse(userId.value())
                .stream()
                .map(UserBookEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserBook> findByUserAndStatus(UserId userId, ReadingStatus status) {
        return userBookJpaRepository.findByUserIdAndStatusAndDeletedFalse(
                userId.value(),
                status
        ).stream()
                .map(UserBookEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByBookAndStatus(BookId bookId, ReadingStatus status) {
        return userBookJpaRepository.countByBookIdAndStatusAndDeletedFalse(
                bookId.getValue(),
                status
        );
    }

    @Override
    public long countByUserAndStatus(UserId userId, ReadingStatus status) {
        return userBookJpaRepository.countByUserIdAndStatusAndDeletedFalse(
                userId.value(),
                status
        );
    }

    @Override
    public long countByUser(UserId userId) {
        return userBookJpaRepository.countByUserIdAndDeletedFalse(userId.value());
    }
}
