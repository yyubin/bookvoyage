package org.yyubin.infrastructure.persistence.review;

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
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewReaction;
import org.yyubin.domain.review.ReviewReactionId;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.user.UserEntity;

@Entity
@Table(
        name = "review_reaction",
        indexes = {
                @Index(name = "idx_reaction_review", columnList = "review_id"),
                @Index(name = "idx_reaction_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_reaction_review_user_content", columnNames = {"review_id", "user_id", "content"})
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, insertable = false, updatable = false)
    private ReviewEntity review;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "content", nullable = false, length = 32)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ReviewReactionEntity fromDomain(ReviewReaction reaction) {
        return ReviewReactionEntity.builder()
                .id(reaction.getId() != null ? reaction.getId().getValue() : null)
                .reviewId(reaction.getReviewId().getValue())
                .userId(reaction.getUserId().value())
                .content(reaction.getContent())
                .createdAt(reaction.getCreatedAt())
                .build();
    }

    public ReviewReaction toDomain() {
        return ReviewReaction.of(
                ReviewReactionId.of(this.id),
                ReviewId.of(this.reviewId),
                new UserId(this.userId),
                this.content,
                this.createdAt
        );
    }
}
