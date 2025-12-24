package org.yyubin.application.bookmark.port;

import java.util.List;
import org.yyubin.domain.bookmark.ReviewBookmark;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

public interface ReviewBookmarkRepository {
    boolean exists(UserId userId, ReviewId reviewId);

    ReviewBookmark save(ReviewBookmark bookmark);

    void delete(UserId userId, ReviewId reviewId);

    List<ReviewBookmark> findByUserAfterCursor(UserId userId, Long cursorId, int size);

    java.util.Optional<ReviewBookmark> findByUserAndReview(UserId userId, ReviewId reviewId);

    long countByUser(UserId userId);
}
