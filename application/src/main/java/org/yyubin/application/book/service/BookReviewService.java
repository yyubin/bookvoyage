package org.yyubin.application.book.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.book.GetBookReviewsUseCase;
import org.yyubin.application.book.dto.BookReviewsResult;
import org.yyubin.application.book.port.SearchBookReviewsPort;
import org.yyubin.application.book.query.GetBookReviewsQuery;
import org.yyubin.application.review.port.ReviewStatisticsPort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookReviewService implements GetBookReviewsUseCase {

    private final SearchBookReviewsPort searchBookReviewsPort;
    private final ReviewStatisticsPort reviewStatisticsPort;

    @Override
    public BookReviewsResult query(GetBookReviewsQuery query) {
        SearchBookReviewsPort.SearchResult searchResult = searchBookReviewsPort.searchByBookId(
                query.bookId(),
                query.cursor(),
                query.size(),
                query.sort()
        );

        // Extract review IDs for batch statistics query
        List<Long> reviewIds = searchResult.reviews().stream()
                .map(SearchBookReviewsPort.ReviewDocument::reviewId)
                .toList();

        // Batch fetch statistics
        Map<Long, ReviewStatisticsPort.ReviewStatistics> statisticsMap =
                reviewStatisticsPort.getBatchStatistics(reviewIds);

        // Combine ES results with statistics
        var reviews = searchResult.reviews().stream()
                .map(doc -> {
                    ReviewStatisticsPort.ReviewStatistics stats = statisticsMap.getOrDefault(
                            doc.reviewId(),
                            new ReviewStatisticsPort.ReviewStatistics(0, 0, 0L)
                    );
                    return new BookReviewsResult.ReviewSummary(
                            doc.reviewId(),
                            doc.userId(),
                            doc.title(),
                            doc.rating(),
                            doc.content(),
                            doc.createdAt(),
                            stats.likeCount(),
                            stats.commentCount(),
                            stats.viewCount()
                    );
                })
                .collect(Collectors.toList());

        return new BookReviewsResult(
                reviews,
                searchResult.nextCursor(),
                searchResult.totalCount()
        );
    }
}
