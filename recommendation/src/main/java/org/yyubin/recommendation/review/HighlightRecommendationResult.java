package org.yyubin.recommendation.review;

import java.util.List;

public record HighlightRecommendationResult(
        List<Long> reviewIds,
        Long nextCursor
) {
}
