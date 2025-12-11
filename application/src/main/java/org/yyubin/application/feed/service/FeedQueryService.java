package org.yyubin.application.feed.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.feed.GetFeedUseCase;
import org.yyubin.application.feed.dto.FeedItemResult;
import org.yyubin.application.feed.dto.FeedPageResult;
import org.yyubin.application.feed.port.FeedItemPort;
import org.yyubin.application.feed.query.GetFeedQuery;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.service.RegisterKeywordsService;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedQueryService implements GetFeedUseCase {

    private final FeedItemPort feedItemPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadBookPort loadBookPort;
    private final RegisterKeywordsService registerKeywordsService;

    @Override
    public FeedPageResult query(GetFeedQuery query) {
        Double cursorScore = query.cursorScore() != null ? query.cursorScore().doubleValue() : null;
        List<FeedItem> feedItems = feedItemPort.loadFeed(new UserId(query.userId()), cursorScore, query.size() + 1);

        List<FeedItemResult> items = feedItems.stream()
                .limit(query.size())
                .map(this::toResult)
                .toList();

        Long nextCursor = feedItems.size() > query.size()
                ? feedItems.get(query.size()).getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                : null;

        return new FeedPageResult(items, nextCursor);
    }

    private FeedItemResult toResult(FeedItem feedItem) {
        Review review = loadReviewPort.loadById(feedItem.getReviewId().getValue());
        Book book = loadBookPort.loadById(review.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));
        ReviewResult reviewResult = ReviewResult.from(review, book, registerKeywordsService.loadKeywords(review.getId()));
        return new FeedItemResult(feedItem.getId(), feedItem.getCreatedAt(), reviewResult);
    }
}
