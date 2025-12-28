package org.yyubin.api.recommendation.dto;

import java.util.List;
import org.yyubin.application.recommendation.dto.BookRecommendationResult;

public record BookRecommendationResponse(
        List<BookRecommendationItemResponse> items,
        int totalItems
) {
    public static BookRecommendationResponse from(List<BookRecommendationResult> results) {
        List<BookRecommendationItemResponse> items = results.stream()
                .map(BookRecommendationItemResponse::from)
                .toList();

        return new BookRecommendationResponse(items, items.size());
    }
}
