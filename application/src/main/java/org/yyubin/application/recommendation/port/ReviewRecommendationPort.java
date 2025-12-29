package org.yyubin.application.recommendation.port;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRecommendationPort {

    List<RecommendationItem> getRecommendations(Long userId, Long cursor, int limit, boolean forceRefresh);

    record RecommendationItem(
            Long reviewId,
            Long bookId,
            Double score,
            Integer rank,
            String source,
            String reason,
            LocalDateTime createdAt
    ) {
    }
}
