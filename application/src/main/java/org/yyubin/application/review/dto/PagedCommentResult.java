package org.yyubin.application.review.dto;

import java.util.List;

public record PagedCommentResult(
        List<ReviewCommentResult> comments,
        Long nextCursor
) {
}
