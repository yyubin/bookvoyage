package org.yyubin.infrastructure.persistence.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.Rating;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.review.Mention;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.review.mention.MentionListConverter;
import org.yyubin.infrastructure.persistence.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, insertable = false, updatable = false)
    private BookEntity book;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private ReviewVisibility visibility;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "view_count", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", length = 50, nullable = false)
    private BookGenre genre;

    @Column(name = "mentions", columnDefinition = "TEXT")
    @Convert(converter = MentionListConverter.class)
    private java.util.List<Mention> mentions;

    public static ReviewEntity fromDomain(Review review) {
        return ReviewEntity.builder()
                .id(review.getId() != null ? review.getId().getValue() : null)
                .userId(review.getUserId().value())
                .bookId(review.getBookId().getValue())
                .rating(review.getRating().getValue())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .visibility(review.getVisibility())
                .deleted(review.isDeleted())
                .viewCount(review.getViewCount())
                .genre(review.getGenre())
                .mentions(review.getMentions())
                .build();
    }

    public Review toDomain() {
        return Review.of(
                ReviewId.of(this.id),
                new UserId(this.userId),
                BookId.of(this.bookId),
                Rating.of(this.rating),
                this.content,
                this.createdAt,
                this.visibility,
                this.deleted,
                this.viewCount != null ? this.viewCount : 0,
                this.genre,
                this.mentions != null ? this.mentions : java.util.Collections.emptyList()
        );
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }
}
