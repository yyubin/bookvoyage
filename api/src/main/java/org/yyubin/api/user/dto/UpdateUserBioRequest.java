package org.yyubin.api.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserBioRequest(
        @NotNull(message = "Bio cannot be null")
        @Size(max = 500, message = "Bio must not exceed 500 characters")
        String bio
) {
}
