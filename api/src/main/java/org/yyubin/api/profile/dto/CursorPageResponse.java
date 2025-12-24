package org.yyubin.api.profile.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        Long nextCursor
) {
}
