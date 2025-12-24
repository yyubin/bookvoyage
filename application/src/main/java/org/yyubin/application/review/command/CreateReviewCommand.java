package org.yyubin.application.review.command;

import java.util.List;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.ReviewVisibility;

public record CreateReviewCommand(
        Long userId,
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
        ReviewVisibility visibility,
        BookGenre genre,
        List<String> keywords,
        List<String> highlights
) {
}
