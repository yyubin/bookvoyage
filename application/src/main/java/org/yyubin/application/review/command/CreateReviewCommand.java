package org.yyubin.application.review.command;

import java.util.List;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.review.BookGenre;

public record CreateReviewCommand(
        Long userId,
        String title,
        String author,
        String isbn,
        String coverUrl,
        String description,
        int rating,
        String content,
        ReviewVisibility visibility,
        BookGenre genre,
        List<String> keywords
) {
}
