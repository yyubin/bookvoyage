package org.yyubin.infrastructure.persistence.wishlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.wishlist.Wishlist;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.user.UserEntity;

@Entity
@Table(
        name = "wishlist",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}),
        indexes = {
                @jakarta.persistence.Index(name = "idx_wishlist_user_id", columnList = "user_id"),
                @jakarta.persistence.Index(name = "idx_wishlist_book_id", columnList = "book_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WishlistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static WishlistEntity fromDomain(Wishlist wishlist, UserEntity user, BookEntity book) {
        return WishlistEntity.builder()
                .id(wishlist.getId())
                .user(user)
                .book(book)
                .createdAt(wishlist.getCreatedAt())
                .build();
    }

    public static WishlistEntity create(UserEntity user, BookEntity book) {
        return WishlistEntity.builder()
                .user(user)
                .book(book)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Wishlist toDomain() {
        return Wishlist.of(
                this.id,
                new UserId(this.user.getId()),
                BookId.of(this.book.getId()),
                this.createdAt
        );
    }
}
