package org.yyubin.application.review;

import java.util.List;

public record PagedCommentResult(
        List<ReviewCommentResult> comments,
        Long nextCursor
) {
}
