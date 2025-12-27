package org.yyubin.application.review.search.query;

import java.time.LocalDate;

public record SearchReviewsQuery(
        // 기본
        String keyword,
        Long cursor,
        Integer size,

        // 필터
        String genre,
        Integer minRating,
        Integer maxRating,
        LocalDate startDate,
        LocalDate endDate,
        String highlight,
        Long bookId,
        Long userId,

        // 정렬
        ReviewSortOption sortBy
) {
    public SearchReviewsQuery {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("keyword must not be blank");
        }
        if (minRating != null && (minRating < 1 || minRating > 5)) {
            throw new IllegalArgumentException("minRating must be between 1 and 5");
        }
        if (maxRating != null && (maxRating < 1 || maxRating > 5)) {
            throw new IllegalArgumentException("maxRating must be between 1 and 5");
        }
        if (minRating != null && maxRating != null && minRating > maxRating) {
            throw new IllegalArgumentException("minRating must be less than or equal to maxRating");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }
    }

    // 하위 호환성을 위한 생성자
    public SearchReviewsQuery(String keyword, Long cursor, Integer size) {
        this(keyword, cursor, size, null, null, null, null, null, null, null, null, null);
    }
}
