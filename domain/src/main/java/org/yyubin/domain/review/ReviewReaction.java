package org.yyubin.domain.review;

import java.time.LocalDateTime;
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
public class ReviewReaction {

    private static final int MAX_CONTENT_LENGTH = 32;

    private final ReviewReactionId id;
    private final ReviewId reviewId;
    private final UserId userId;
    private final String content;
    private final LocalDateTime createdAt;

    public static ReviewReaction of(
            ReviewReactionId id,
            ReviewId reviewId,
            UserId userId,
            String content,
            LocalDateTime createdAt
    ) {
        Objects.requireNonNull(reviewId, "Review ID cannot be null");
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");
        validateContent(content);

        return new ReviewReaction(id, reviewId, userId, content, createdAt);
    }

    public static ReviewReaction create(ReviewId reviewId, UserId userId, String content) {
        return of(null, reviewId, userId, content, LocalDateTime.now());
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Reaction content cannot be empty");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Reaction content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }
    }
}
