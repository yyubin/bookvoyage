package org.yyubin.application.review.search.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.yyubin.application.review.search.query.ReviewSortOption;
import org.yyubin.application.review.search.query.SearchReviewsQuery;

public record ReviewSearchFilter(
        String keyword,
        Long cursor,
        int size,
        String genre,
        Integer minRating,
        Integer maxRating,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String highlightNorm,
        Long bookId,
        Long userId,
        ReviewSortOption sortBy
) {
    public static ReviewSearchFilter from(SearchReviewsQuery query, int resolvedSize) {
        return new ReviewSearchFilter(
                query.keyword(),
                query.cursor(),
                resolvedSize,
                query.genre(),
                query.minRating(),
                query.maxRating(),
                toDateTime(query.startDate()),
                toDateTime(query.endDate()),
                normalizeHighlight(query.highlight()),
                query.bookId(),
                query.userId(),
                query.sortBy() != null ? query.sortBy() : ReviewSortOption.RELEVANCE
        );
    }

    private static LocalDateTime toDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    private static String normalizeHighlight(String highlight) {
        if (highlight == null || highlight.isBlank()) {
            return null;
        }
        return highlight.trim();
    }
}
