package org.yyubin.api.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.ReviewVisibility;

public record ReviewResponse(
        Long reviewId,
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
        int rating,
        String summary,
        String content,
        LocalDateTime createdAt,
        ReviewVisibility visibility,
        boolean deleted,
        long viewCount,
        BookGenre genre,
        List<String> keywords,
        List<String> highlights,
        List<MentionResponse> mentions
) {

    public static ReviewResponse from(ReviewResult result) {
        return new ReviewResponse(
                result.reviewId(),
                result.bookId(),
                result.title(),
                result.authors(),
                result.isbn10(),
                result.isbn13(),
                result.coverUrl(),
                result.publisher(),
                result.publishedDate(),
                result.description(),
                result.language(),
                result.pageCount(),
                result.googleVolumeId(),
                result.rating(),
                result.summary(),
                result.content(),
                result.createdAt(),
                result.visibility(),
                result.deleted(),
                result.viewCount(),
                result.genre(),
                result.keywords(),
                result.highlights(),
                result.mentions().stream().map(MentionResponse::from).toList()
        );
    }
}
