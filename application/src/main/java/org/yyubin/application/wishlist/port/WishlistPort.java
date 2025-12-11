package org.yyubin.application.wishlist.port;

import java.util.List;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.wishlist.Wishlist;

public interface WishlistPort {
    boolean exists(UserId userId, BookId bookId);

    Wishlist save(Wishlist wishlist);

    void delete(UserId userId, BookId bookId);

    List<Wishlist> findByUser(UserId userId);
}
