package org.yyubin.api.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateReviewRequest(
        @NotBlank(message = "Book title is required")
        String title,

        @NotEmpty(message = "At least one author is required")
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

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        int rating,

        @Size(max = 5000, message = "Review content must not exceed 5000 characters")
        String content,

        String visibility,

        @NotBlank(message = "Genre is required")
        String genre,

        List<String> keywords
) {
}
