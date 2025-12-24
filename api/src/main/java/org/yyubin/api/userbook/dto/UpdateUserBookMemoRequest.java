package org.yyubin.api.userbook.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserBookMemoRequest(
        @Size(max = 2000, message = "memo cannot exceed 2000 characters")
        String memo
) {
}
