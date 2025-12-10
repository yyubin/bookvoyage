package org.yyubin.application.review;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.query.GetReviewQuery;
import org.yyubin.application.review.query.GetUserReviewsQuery;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.Mention;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService implements GetReviewUseCase, GetUserReviewsUseCase {

    private final LoadReviewPort loadReviewPort;
    private final LoadBookPort loadBookPort;
    private final RegisterKeywordsService registerKeywordsService;

    @Override
    public ReviewResult query(GetReviewQuery query) {
        Review review = loadReviewPort.loadById(query.reviewId());
        validateViewPermission(review, query.viewerId());
        Book book = loadBookPort.loadById(review.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));

        return ReviewResult.from(review, book, registerKeywordsService.loadKeywords(ReviewId.of(review.getId().getValue())));
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

    private ReviewResult toResult(Review review) {
        Book book = loadBookPort.loadById(review.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));
        return ReviewResult.from(review, book, registerKeywordsService.loadKeywords(review.getId()));
    }

    private void validateViewPermission(Review review, Long viewerId) {
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }

        if (review.getVisibility().isPublic()) {
            return;
        }

        if (viewerId == null) {
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }

        if (!review.isWrittenBy(new UserId(viewerId))) {
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }
    }
}
