package org.yyubin.infrastructure.persistence.review.bookmark;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
import org.yyubin.domain.bookmark.ReviewBookmark;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.user.UserEntity;

@Entity
@Table(
        name = "review_bookmark",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}),
        indexes = {
                @Index(name = "idx_review_bookmark_user_id", columnList = "user_id"),
                @Index(name = "idx_review_bookmark_review_id", columnList = "review_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewBookmarkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ReviewEntity review;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ReviewBookmarkEntity fromDomain(ReviewBookmark bookmark, UserEntity user, ReviewEntity review) {
        return ReviewBookmarkEntity.builder()
                .id(bookmark.id())
                .user(user)
                .review(review)
                .createdAt(bookmark.createdAt())
                .build();
    }

    public ReviewBookmark toDomain() {
        return new ReviewBookmark(
                this.id,
                new UserId(this.user.getId()),
                ReviewId.of(this.review.getId()),
                this.createdAt
        );
    }
}
