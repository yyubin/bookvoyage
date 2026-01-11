package org.yyubin.application.review.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.review.CheckUserReviewUseCase;
import org.yyubin.application.review.GetReviewUseCase;
import org.yyubin.application.review.GetReviewsByHighlightUseCase;
import org.yyubin.application.review.GetUserReviewsUseCase;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.dto.PagedReviewResult;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewExistencePort;
import org.yyubin.application.review.port.ReviewViewMetricPort;
import org.yyubin.application.review.port.ReviewReactionPort;
import org.yyubin.application.review.query.CheckUserReviewQuery;
import org.yyubin.application.review.query.GetReviewQuery;
import org.yyubin.application.review.query.GetReviewsByHighlightQuery;
import org.yyubin.application.review.query.GetUserReviewsQuery;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.domain.user.UserId;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService implements GetReviewUseCase, GetUserReviewsUseCase, GetReviewsByHighlightUseCase, CheckUserReviewUseCase {

    private final LoadReviewPort loadReviewPort;
    private final LoadBookPort loadBookPort;
    private final LoadKeywordsUseCase loadKeywordsUseCase;
    private final LoadHighlightsUseCase loadHighlightsUseCase;
    private final ReviewViewMetricPort reviewViewMetricPort;
    private final ReviewExistencePort reviewExistencePort;
    private final ReviewReactionPort reviewReactionPort;
    private final org.yyubin.application.bookmark.port.ReviewBookmarkRepository reviewBookmarkRepository;
    private final EventPublisher eventPublisher;
    private final HighlightNormalizer highlightNormalizer;
    private final org.yyubin.application.user.port.LoadUserPort loadUserPort;
    private final org.yyubin.application.review.port.ReviewLikePort reviewLikePort;

    @Override
    public ReviewResult query(GetReviewQuery query) {
        log.debug("Querying review {} with viewerId: {}", query.reviewId(), query.viewerId());
        Review review = loadReviewPort.loadById(query.reviewId());
        log.debug("Loaded review {} - visibility: {}, deleted: {}",
            review.getId().getValue(), review.getVisibility(), review.isDeleted());
        validateViewPermission(review, query.viewerId());
        log.debug("View permission validated for review {}", review.getId().getValue());
        Book book = loadBookPort.loadById(review.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));
        org.yyubin.domain.user.User author = loadUserPort.loadById(review.getUserId());

        long cachedView = reviewViewMetricPort.incrementAndGet(review.getId().getValue(), query.viewerId());
        publishViewEvent(review, book, cachedView, query.viewerId());

        long viewForResponse = reviewViewMetricPort.getCachedCount(review.getId().getValue())
                .orElse(review.getViewCount());

        // 북마크 여부 조회 (로그인한 경우만)
        boolean bookmarked = false;
        if (query.viewerId() != null) {
            bookmarked = reviewBookmarkRepository.exists(
                    new UserId(query.viewerId()),
                    ReviewId.of(review.getId().getValue())
            );
        }

        // 리액션 집계 조회
        var reactionCounts = reviewReactionPort.countByReviewIdGroupByContent(review.getId().getValue());
        List<ReviewResult.ReactionSummary> reactions = reactionCounts.stream()
                .map(rc -> new ReviewResult.ReactionSummary(rc.getEmoji(), rc.getCount()))
                .collect(Collectors.toList());

        // 사용자의 리액션 조회 (로그인한 경우만)
        String userReaction = null;
        if (query.viewerId() != null) {
            userReaction = reviewReactionPort.loadByReviewIdAndUserId(review.getId().getValue(), query.viewerId())
                    .map(reaction -> reaction.getContent())
                    .orElse(null);
        }

        // 좋아요 여부 및 개수 조회
        boolean isLiked = false;
        if (query.viewerId() != null) {
            isLiked = reviewLikePort.exists(review.getId(), new UserId(query.viewerId()));
        }
        long likeCount = reviewLikePort.countByReviewId(review.getId());

        return ReviewResult.fromWithViewCountAndInteraction(
                review,
                book,
                author,
                loadKeywordsUseCase.loadKeywords(ReviewId.of(review.getId().getValue())),
                loadHighlightsUseCase.loadHighlights(ReviewId.of(review.getId().getValue())),
                viewForResponse,
                bookmarked,
                reactions,
                userReaction,
                isLiked,
                likeCount
        );
    }

    @Override
    public boolean query(CheckUserReviewQuery query) {
        if (query.userId() == null || query.bookId() == null) {
            return false;
        }
        return reviewExistencePort.existsByUserAndBook(
            new UserId(query.userId()),
            BookId.of(query.bookId())
        );
    }

    private void publishViewEvent(Review review, Book book, long cachedView, Long viewerId) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("reviewId", review.getId().getValue());
        metadata.put("bookId", book.getId().getValue());
        metadata.put("viewCount", cachedView);

        eventPublisher.publish(
                EventTopics.REVIEW,
                viewerId != null ? viewerId.toString() : "anonymous",
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        "REVIEW_VIEWED",
                        viewerId,
                        "REVIEW",
                        review.getId().getValue().toString(),
                        metadata,
                        java.time.Instant.now(),
                        "api",
                        1
                )
        );
    }

    @Override
    public PagedReviewResult query(GetUserReviewsQuery query) {
        List<Review> reviews = loadReviewPort.loadByUserId(query.userId(), query.viewerId(), query.cursor(), query.size() + 1);

        List<ReviewResult> mapped = reviews.stream()
                .limit(query.size())
                .map(this::toResult)
                .collect(Collectors.toList());

        Long nextCursor = reviews.size() > query.size()
                ? reviews.get(query.size()).getId().getValue()
                : null;

        return new PagedReviewResult(mapped, nextCursor);
    }

    @Override
    public PagedReviewResult query(GetReviewsByHighlightQuery query) {
        if (query.highlight() == null || query.highlight().isBlank()) {
            throw new IllegalArgumentException("Highlight must not be empty");
        }
        String normalized = highlightNormalizer.normalize(query.highlight());
        List<Review> reviews = loadReviewPort.loadByHighlightNormalized(normalized, query.cursor(), query.size() + 1);

        List<ReviewResult> mapped = reviews.stream()
                .limit(query.size())
                .map(this::toResult)
                .collect(Collectors.toList());

        Long nextCursor = reviews.size() > query.size()
                ? reviews.get(query.size()).getId().getValue()
                : null;

        return new PagedReviewResult(mapped, nextCursor);
    }

    private ReviewResult toResult(Review review) {
        Book book = loadBookPort.loadById(review.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));
        org.yyubin.domain.user.User author = loadUserPort.loadById(review.getUserId());
        long viewForResponse = reviewViewMetricPort.getCachedCount(review.getId().getValue())
                .orElse(review.getViewCount());
        return ReviewResult.fromWithViewCount(
                review,
                book,
                author,
                loadKeywordsUseCase.loadKeywords(review.getId()),
                loadHighlightsUseCase.loadHighlights(review.getId()),
                viewForResponse
        );
    }

    private void validateViewPermission(Review review, Long viewerId) {
        if (review.isDeleted()) {
            log.warn("Access denied: Review {} is deleted", review.getId().getValue());
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }

        if (review.getVisibility().isPublic()) {
            log.debug("Review {} is public - access granted", review.getId().getValue());
            return;
        }

        if (viewerId == null) {
            log.warn("Access denied: Review {} is private and viewerId is null", review.getId().getValue());
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }

        if (!review.isWrittenBy(new UserId(viewerId))) {
            log.warn("Access denied: Review {} - viewer {} is not the author",
                review.getId().getValue(), viewerId);
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }
    }
}
