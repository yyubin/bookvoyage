package org.yyubin.api.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReactionRequest(
        @NotBlank(message = "Reaction content is required")
        @Size(max = 32, message = "Reaction cannot exceed 32 characters")
        String content
) {
}
