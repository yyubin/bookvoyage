package org.yyubin.infrastructure.persistence.wishlist;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.wishlist.port.WishlistPort;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.wishlist.Wishlist;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistPersistenceAdapter implements WishlistPort {

    private final WishlistJpaRepository wishlistJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    @Override
    public boolean exists(UserId userId, BookId bookId) {
        UserEntity user = userJpaRepository.getReferenceById(userId.value());
        BookEntity book = bookJpaRepository.getReferenceById(bookId.getValue());
        return wishlistJpaRepository.existsByUserAndBook(user, book);
    }

    @Override
    @Transactional
    public Wishlist save(Wishlist wishlist) {
        UserEntity user = userJpaRepository.getReferenceById(wishlist.getUserId().value());
        BookEntity book = bookJpaRepository.getReferenceById(wishlist.getBookId().getValue());

        WishlistEntity saved = wishlistJpaRepository.save(WishlistEntity.fromDomain(wishlist, user, book));
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void delete(UserId userId, BookId bookId) {
        wishlistJpaRepository.deleteByUserIdAndBookId(userId.value(), bookId.getValue());
    }

    @Override
    public List<Wishlist> findByUser(UserId userId) {
        return wishlistJpaRepository.findByUserId(userId.value())
                .stream()
                .map(WishlistEntity::toDomain)
                .toList();
    }
}
