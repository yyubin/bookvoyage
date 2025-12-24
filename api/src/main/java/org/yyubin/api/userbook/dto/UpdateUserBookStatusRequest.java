package org.yyubin.api.userbook.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserBookStatusRequest(
        @NotBlank(message = "status is required")
        String status
) {
}
