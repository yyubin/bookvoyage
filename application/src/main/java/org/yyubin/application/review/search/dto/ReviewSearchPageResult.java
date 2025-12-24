package org.yyubin.application.review.search.dto;

import java.util.List;

public record ReviewSearchPageResult(
        List<ReviewSearchItemResult> items,
        Long nextCursor
) {
}
