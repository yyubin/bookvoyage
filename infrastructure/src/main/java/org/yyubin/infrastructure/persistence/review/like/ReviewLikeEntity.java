package org.yyubin.infrastructure.persistence.review.like;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewLike;
import org.yyubin.domain.review.ReviewLikeId;
import org.yyubin.domain.user.UserId;

@Entity
@Table(name = "review_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"review_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReviewLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ReviewLikeEntity fromDomain(ReviewLike reviewLike) {
        return new ReviewLikeEntity(
                reviewLike.getId() != null ? reviewLike.getId().value() : null,
                reviewLike.getReviewId().getValue(),
                reviewLike.getUserId().value(),
                reviewLike.getCreatedAt()
        );
    }

    public ReviewLike toDomain() {
        return ReviewLike.reconstruct(
                ReviewLikeId.of(id),
                ReviewId.of(reviewId),
                new UserId(userId),
                createdAt
        );
    }
}
