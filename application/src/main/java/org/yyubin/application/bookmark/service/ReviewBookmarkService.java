package org.yyubin.application.bookmark.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.bookmark.AddBookmarkUseCase;
import org.yyubin.application.bookmark.GetBookmarksUseCase;
import org.yyubin.application.bookmark.RemoveBookmarkUseCase;
import org.yyubin.application.bookmark.command.AddBookmarkCommand;
import org.yyubin.application.bookmark.command.RemoveBookmarkCommand;
import org.yyubin.application.bookmark.dto.ReviewBookmarkItem;
import org.yyubin.application.bookmark.dto.ReviewBookmarkPageResult;
import org.yyubin.application.bookmark.port.ReviewBookmarkRepository;
import org.yyubin.application.bookmark.query.GetBookmarksQuery;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.bookmark.ReviewBookmark;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
public class ReviewBookmarkService implements AddBookmarkUseCase, RemoveBookmarkUseCase, GetBookmarksUseCase {

    private final ReviewBookmarkRepository reviewBookmarkRepository;
    private final LoadReviewPort loadReviewPort;
    private final LoadBookPort loadBookPort;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewBookmark add(AddBookmarkCommand command) {
        UserId userId = new UserId(command.userId());
        ReviewId reviewId = ReviewId.of(command.reviewId());
        Review review = loadReviewPort.loadById(reviewId.getValue());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + reviewId.getValue());
        }

        ReviewBookmark bookmark = reviewBookmarkRepository.findByUserAndReview(userId, reviewId)
                .orElseGet(() -> reviewBookmarkRepository.save(ReviewBookmark.create(userId, reviewId)));
        publishBookmarkEvent("BOOKMARK_ADD", bookmark);
        return bookmark;
    }

    @Override
    @Transactional
    public void remove(RemoveBookmarkCommand command) {
        UserId userId = new UserId(command.userId());
        ReviewId reviewId = ReviewId.of(command.reviewId());
        reviewBookmarkRepository.delete(userId, reviewId);
        publishBookmarkEvent("BOOKMARK_REMOVE", new ReviewBookmark(null, userId, reviewId, java.time.LocalDateTime.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewBookmarkPageResult query(GetBookmarksQuery query) {
        UserId userId = new UserId(query.userId());
        List<ReviewBookmark> bookmarks = reviewBookmarkRepository.findByUserAfterCursor(userId, query.cursorId(), query.size() + 1);

        List<ReviewBookmarkItem> items = bookmarks.stream()
                .limit(query.size())
                .map(bookmark -> toItem(bookmark))
                .toList();

        Long nextCursor = bookmarks.size() > query.size()
                ? bookmarks.get(query.size()).id()
                : null;

        return new ReviewBookmarkPageResult(items, nextCursor);
    }

    private ReviewBookmarkItem toItem(ReviewBookmark bookmark) {
        Review review = loadReviewPort.loadById(bookmark.reviewId().getValue());
        Book book = loadBookPort.loadById(review.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));

        return ReviewBookmarkItem.from(review, book, bookmark.id(), bookmark.createdAt());
    }

    private void publishBookmarkEvent(String actionType, ReviewBookmark bookmark) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("actionType", actionType);
        metadata.put("reviewId", bookmark.reviewId().getValue());
        metadata.put("bookId", loadReviewBookId(bookmark.reviewId().getValue()));
        eventPublisher.publish(
                EventTopics.WISHLIST_BOOKMARK,
                bookmark.userId().value().toString(),
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        actionType,
                        bookmark.userId().value(),
                        "REVIEW",
                        bookmark.reviewId().getValue().toString(),
                        metadata,
                        java.time.Instant.now(),
                        "api",
                        1
                )
        );
    }

    private Long loadReviewBookId(Long reviewId) {
        Review review = loadReviewPort.loadById(reviewId);
        return review.getBookId().getValue();
    }
}
