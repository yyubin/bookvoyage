package org.yyubin.infrastructure.persistence.wishlist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.user.UserEntity;

@Repository
public interface WishlistJpaRepository extends JpaRepository<WishlistEntity, Long> {

    boolean existsByUserAndBook(UserEntity user, BookEntity book);

    void deleteByUserIdAndBookId(Long userId, Long bookId);

    List<WishlistEntity> findByUserId(Long userId);

    long countByBookId(Long bookId);

    long countByUserId(Long userId);
}
