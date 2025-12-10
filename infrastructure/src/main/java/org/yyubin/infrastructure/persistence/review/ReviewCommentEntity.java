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
import jakarta.persistence.Convert;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.review.ReviewComment;
import org.yyubin.domain.review.ReviewCommentId;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.Mention;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.user.UserEntity;

@Entity
@Table(
        name = "review_comment",
        indexes = {
                @Index(name = "idx_review_comment_review", columnList = "review_id"),
                @Index(name = "idx_review_comment_parent", columnList = "parent_comment_id")
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewCommentEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", insertable = false, updatable = false)
    private ReviewCommentEntity parent;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "mentions", columnDefinition = "TEXT")
    @Convert(converter = MentionListConverter.class)
    private java.util.List<Mention> mentions;

    public static ReviewCommentEntity fromDomain(ReviewComment comment) {
        return ReviewCommentEntity.builder()
                .id(comment.getId() != null ? comment.getId().getValue() : null)
                .reviewId(comment.getReviewId().getValue())
                .userId(comment.getUserId().value())
                .parentCommentId(comment.getParentId() != null ? comment.getParentId().getValue() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .editedAt(comment.getEditedAt())
                .deleted(comment.isDeleted())
                .mentions(comment.getMentions())
                .build();
    }

    public ReviewComment toDomain() {
        return ReviewComment.of(
                ReviewCommentId.of(this.id),
                ReviewId.of(this.reviewId),
                new UserId(this.userId),
                this.content,
                this.parentCommentId != null ? ReviewCommentId.of(this.parentCommentId) : null,
                this.createdAt,
                this.editedAt,
                this.deleted,
                this.mentions != null ? this.mentions : java.util.Collections.emptyList()
        );
    }
}
