package org.yyubin.domain.review;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.yyubin.domain.user.UserId;

@Getter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewComment {

    private static final int MAX_LENGTH = 2000;

    private final ReviewCommentId id;
    private final ReviewId reviewId;
    private final UserId userId;
    private final String content;
    private final ReviewCommentId parentId;
    private final LocalDateTime createdAt;
    private final LocalDateTime editedAt;
    private final boolean deleted;
    private final List<Mention> mentions;

    public static ReviewComment of(
            ReviewCommentId id,
            ReviewId reviewId,
            UserId userId,
            String content,
            ReviewCommentId parentId,
            LocalDateTime createdAt,
            LocalDateTime editedAt,
            boolean deleted,
            List<Mention> mentions
    ) {
        Objects.requireNonNull(reviewId, "Review ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");
        Objects.requireNonNull(mentions, "Mentions cannot be null");
        validateContent(content);
        if (parentId != null && parentId.equals(id)) {
            throw new IllegalArgumentException("Comment cannot reference itself as parent");
        }

        return new ReviewComment(
                id,
                reviewId,
                userId,
                content != null ? content : "",
                parentId,
                createdAt,
                editedAt,
                deleted,
                List.copyOf(mentions)
        );
    }

    public static ReviewComment create(ReviewId reviewId, UserId userId, String content, ReviewCommentId parentId, List<Mention> mentions) {
        return of(null, reviewId, userId, content, parentId, LocalDateTime.now(), null, false, mentions != null ? mentions : Collections.emptyList());
    }

    public ReviewComment updateContent(String newContent, List<Mention> newMentions) {
        validateContent(newContent);
        Objects.requireNonNull(newMentions, "Mentions cannot be null");
        return new ReviewComment(
                this.id,
                this.reviewId,
                this.userId,
                newContent,
                this.parentId,
                this.createdAt,
                LocalDateTime.now(),
                this.deleted,
                List.copyOf(newMentions)
        );
    }

    public ReviewComment markDeleted() {
        return new ReviewComment(
                this.id,
                this.reviewId,
                this.userId,
                this.content,
                this.parentId,
                this.createdAt,
                this.editedAt,
                true,
                this.mentions
        );
    }

    public boolean isOwnedBy(UserId ownerId) {
        return this.userId.equals(ownerId);
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        if (content.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Comment content cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
