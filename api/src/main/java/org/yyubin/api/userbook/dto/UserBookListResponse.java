package org.yyubin.api.userbook.dto;

import java.util.List;
import org.yyubin.application.userbook.dto.UserBookListResult;

public record UserBookListResponse(
        List<UserBookResponse> items
) {
    public static UserBookListResponse from(UserBookListResult result) {
        return new UserBookListResponse(
                result.items().stream()
                        .map(UserBookResponse::from)
                        .toList()
        );
    }
}
