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
    private final org.yyubin.application.user.port.LoadUserPort loadUserPort;

    @Override
    public BookReviewsResult query(GetBookReviewsQuery query) {
        SearchBookReviewsPort.SearchResult searchResult = searchBookReviewsPort.searchByBookId(
                query.bookId(),
                query.cursor(),
                query.size(),
                query.sort()
        );

        // Extract review IDs and user IDs for batch queries
        List<Long> reviewIds = searchResult.reviews().stream()
                .map(SearchBookReviewsPort.ReviewDocument::reviewId)
                .toList();

        List<Long> userIds = searchResult.reviews().stream()
                .map(SearchBookReviewsPort.ReviewDocument::userId)
                .distinct()
                .toList();

        // Batch fetch statistics
        Map<Long, ReviewStatisticsPort.ReviewStatistics> statisticsMap =
                reviewStatisticsPort.getBatchStatistics(reviewIds);

        // Batch fetch user information
        Map<Long, org.yyubin.domain.user.User> userMap = loadUserPort.loadByIdsBatch(userIds);

        // Combine ES results with statistics and user info
        var reviews = searchResult.reviews().stream()
                .map(doc -> {
                    ReviewStatisticsPort.ReviewStatistics stats = statisticsMap.getOrDefault(
                            doc.reviewId(),
                            new ReviewStatisticsPort.ReviewStatistics(0, 0, 0L)
                    );
                    org.yyubin.domain.user.User user = userMap.get(doc.userId());
                    String authorNickname = user != null ? user.nickname() : "Unknown";

                    return new BookReviewsResult.ReviewSummary(
                            doc.reviewId(),
                            doc.userId(),
                            authorNickname,
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
