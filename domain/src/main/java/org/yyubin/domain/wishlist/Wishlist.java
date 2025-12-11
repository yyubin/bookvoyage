package org.yyubin.domain.wishlist;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

@Getter
@EqualsAndHashCode(of = {"userId", "bookId"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Wishlist {

    private final Long id;
    private final UserId userId;
    private final BookId bookId;
    private final LocalDateTime createdAt;

    public static Wishlist of(Long id, UserId userId, BookId bookId, LocalDateTime createdAt) {
        return new Wishlist(
                id,
                Objects.requireNonNull(userId, "userId must not be null"),
                Objects.requireNonNull(bookId, "bookId must not be null"),
                Objects.requireNonNull(createdAt, "createdAt must not be null")
        );
    }

    public static Wishlist create(UserId userId, BookId bookId) {
        return of(null, userId, bookId, LocalDateTime.now());
    }
}
