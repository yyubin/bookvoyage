package org.yyubin.api.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateReviewRequest(
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating cannot exceed 5")
        Integer rating,

        @Size(max = 5000, message = "Review content must not exceed 5000 characters")
        String content,

        String visibility,

        String genre,

        List<String> keywords
) {
}
