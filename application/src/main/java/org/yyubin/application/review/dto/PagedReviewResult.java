package org.yyubin.application.review.dto;

import java.util.List;

public record PagedReviewResult(
        List<ReviewResult> reviews,
        Long nextCursor
) {
}
