package org.yyubin.infrastructure.persistence.feed;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "feed_item")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, insertable = false, updatable = false)
    private ReviewEntity review;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static FeedItemEntity fromDomain(FeedItem feedItem) {
        return FeedItemEntity.builder()
                .id(feedItem.getId())
                .userId(feedItem.getUserId().value())
                .reviewId(feedItem.getReviewId().getValue())
                .createdAt(feedItem.getCreatedAt())
                .build();
    }

    public FeedItem toDomain() {
        return FeedItem.of(
                this.id,
                new UserId(this.userId),
                ReviewId.of(this.reviewId),
                this.createdAt
        );
    }
}
