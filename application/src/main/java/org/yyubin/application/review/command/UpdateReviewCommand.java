package org.yyubin.application.review.command;

import org.yyubin.domain.review.ReviewVisibility;
import java.util.List;
import org.yyubin.domain.review.BookGenre;

public record UpdateReviewCommand(
        Long reviewId,
        Long userId,
        Integer rating,
        String content,
        ReviewVisibility visibility,
        BookGenre genre,
        List<String> keywords
) {
}
