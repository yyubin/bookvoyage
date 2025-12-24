package org.yyubin.api.userbook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateUserBookProgressRequest(
        @Min(value = 0, message = "progress must be at least 0")
        @Max(value = 100, message = "progress cannot exceed 100")
        int progress
) {
}
