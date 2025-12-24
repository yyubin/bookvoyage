package org.yyubin.infrastructure.persistence.userbook;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.*;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.user.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_book",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_book__user_id_book_id",
        columnNames = {"user_id", "book_id"}
    ),
    indexes = {
        @Index(name = "idx_user_book__user_id", columnList = "user_id"),
        @Index(name = "idx_user_book__book_id", columnList = "book_id"),
        @Index(name = "idx_user_book__status", columnList = "status"),
        @Index(name = "idx_user_book__user_status", columnList = "user_id, status"),
        @Index(name = "idx_user_book__deleted", columnList = "deleted"),
        @Index(name = "idx_user_book__user_deleted", columnList = "user_id, deleted")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserBookEntity {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReadingStatus status;

    @Column(name = "progress_percentage", nullable = false)
    private Integer progressPercentage;

    @Column(name = "personal_rating")
    private Integer personalRating;

    @Column(name = "personal_memo", columnDefinition = "TEXT")
    private String personalMemo;

    @Column(name = "reading_count", nullable = false)
    private Integer readingCount;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserBookEntity fromDomain(UserBook userBook) {
        return UserBookEntity.builder()
                .id(userBook.getId())
                .userId(userBook.getUserId().value())
                .bookId(userBook.getBookId().getValue())
                .status(userBook.getStatus())
                .progressPercentage(userBook.getProgress().getPercentage())
                .personalRating(userBook.getRating().getValue())
                .personalMemo(userBook.getMemo().getContent())
                .readingCount(userBook.getReadingCount().getCount())
                .startDate(userBook.getStartDate())
                .completionDate(userBook.getCompletionDate())
                .deleted(userBook.isDeleted())
                .deletedAt(userBook.getDeletedAt())
                .createdAt(userBook.getCreatedAt())
                .updatedAt(userBook.getUpdatedAt())
                .build();
    }

    public UserBook toDomain() {
        return UserBook.of(
                this.id,
                new UserId(this.userId),
                BookId.of(this.bookId),
                this.status,
                ReadingProgress.of(this.progressPercentage),
                PersonalRating.of(this.personalRating),
                PersonalMemo.of(this.personalMemo),
                ReadingCount.of(this.readingCount),
                this.startDate,
                this.completionDate,
                this.deleted,
                this.deletedAt,
                this.createdAt,
                this.updatedAt
        );
    }
}
