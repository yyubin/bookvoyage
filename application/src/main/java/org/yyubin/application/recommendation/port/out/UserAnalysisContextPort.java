package org.yyubin.application.recommendation.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated user context for AI preference analysis.
 */
public interface UserAnalysisContextPort {

    UserAnalysisContext loadContext(Long userId, int reviewLimit, int libraryLimit, int searchLimit, LocalDateTime searchSince);

    record UserAnalysisContext(
        Long userId,
        List<ReviewSnapshot> recentReviews,
        List<UserBookSnapshot> recentLibraryUpdates,
        List<String> recentSearchQueries
    ) {}

    record ReviewSnapshot(
        Long reviewId,
        Long bookId,
        String bookTitle,
        List<String> bookAuthors,
        Integer rating,
        String genre,
        String summary,
        List<String> keywords,
        LocalDateTime createdAt
    ) {}

    record UserBookSnapshot(
        Long bookId,
        String bookTitle,
        List<String> bookAuthors,
        String status,
        Integer rating,
        String memo,
        LocalDateTime updatedAt
    ) {}
}
