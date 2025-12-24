package org.yyubin.application.userbook.port;

import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.domain.userbook.UserBook;

import java.util.List;
import java.util.Optional;

public interface UserBookPort {

    Optional<UserBook> findByUserAndBook(UserId userId, BookId bookId);

    boolean exists(UserId userId, BookId bookId);

    UserBook save(UserBook userBook);

    void delete(UserId userId, BookId bookId);

    List<UserBook> findByUser(UserId userId);

    List<UserBook> findByUserAndStatus(UserId userId, ReadingStatus status);

    List<UserBook> findLatestByUserAndStatus(UserId userId, ReadingStatus status, int size);
}
