package org.yyubin.api.user.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserProfileImageRequest(
        @NotNull(message = "Image URL cannot be null")
        String imageUrl
) {
}
