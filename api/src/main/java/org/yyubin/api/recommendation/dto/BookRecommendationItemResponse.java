package org.yyubin.api.recommendation.dto;

import java.util.Collections;
import java.util.List;
import org.yyubin.application.recommendation.dto.BookRecommendationResult;

public record BookRecommendationItemResponse(
        Long bookId,
        String title,
        List<String> authors,
        String isbn10,
        String isbn13,
        String coverUrl,
        String publisher,
        String publishedDate,
        String description,
        String language,
        Integer pageCount,
        String googleVolumeId,
        Double score,
        Integer rank,
        String source,
        String reason
) {
    public static BookRecommendationItemResponse from(BookRecommendationResult result) {
        return new BookRecommendationItemResponse(
                result.bookId(),
                result.title(),
                result.authors() == null ? Collections.emptyList() : result.authors(),
                result.isbn10(),
                result.isbn13(),
                result.coverUrl(),
                result.publisher(),
                result.publishedDate(),
                result.description(),
                result.language(),
                result.pageCount(),
                result.googleVolumeId(),
                result.score(),
                result.rank(),
                result.source(),
                result.reason()
        );
    }
}
