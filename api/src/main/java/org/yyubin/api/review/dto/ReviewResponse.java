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
        String author,
        String isbn,
        String coverUrl,
        String description,
        int rating,
        String content,
        LocalDateTime createdAt,
        ReviewVisibility visibility,
        boolean deleted,
        long viewCount,
        BookGenre genre,
        List<String> keywords,
        List<MentionResponse> mentions
) {

    public static ReviewResponse from(ReviewResult result) {
        return new ReviewResponse(
                result.reviewId(),
                result.bookId(),
                result.title(),
                result.author(),
                result.isbn(),
                result.coverUrl(),
                result.description(),
                result.rating(),
                result.content(),
                result.createdAt(),
                result.visibility(),
                result.deleted(),
                result.viewCount(),
                result.genre(),
                result.keywords(),
                result.mentions().stream().map(MentionResponse::from).toList()
        );
    }
}
