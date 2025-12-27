package org.yyubin.application.review.search.query;

public enum ReviewSortOption {
    RELEVANCE,      // 관련도순 (Elasticsearch _score)
    LATEST,         // 최신순 (createdAt desc)
    RATING_DESC,    // 평점 높은순
    RATING_ASC;     // 평점 낮은순

    public static ReviewSortOption from(String value) {
        if (value == null || value.isBlank()) {
            return RELEVANCE;
        }
        try {
            return ReviewSortOption.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RELEVANCE;
        }
    }
}
