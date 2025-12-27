package org.yyubin.api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileImageUploadRequest(
        @NotBlank(message = "Filename cannot be blank")
        String filename
) {
}
