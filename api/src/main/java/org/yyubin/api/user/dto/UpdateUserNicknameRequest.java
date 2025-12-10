package org.yyubin.api.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserNicknameRequest(
        @NotNull(message = "Nickname cannot be null")
        @Size(max = 30, message = "Nickname must not exceed 30 characters")
        String nickname
) {
}
