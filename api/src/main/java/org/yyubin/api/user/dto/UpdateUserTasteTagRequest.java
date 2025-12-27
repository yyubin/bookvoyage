package org.yyubin.api.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserTasteTagRequest(
        @NotNull(message = "Taste tag cannot be null")
        @Size(max = 100, message = "Taste tag must not exceed 100 characters")
        String tasteTag
) {
}
