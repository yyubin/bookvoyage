package org.yyubin.application.recommendation.port;

import java.util.List;

public interface BookRecommendationPort {

    List<RecommendationItem> getRecommendations(Long userId, int limit, boolean forceRefresh);

    record RecommendationItem(
            Long bookId,
            Double score,
            Integer rank,
            String source,
            String reason
    ) {
    }
}
