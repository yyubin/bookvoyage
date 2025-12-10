package org.yyubin.application.review;

import java.util.List;

public record PagedReviewResult(
        List<ReviewResult> reviews,
        Long nextCursor
) {
}
