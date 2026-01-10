package org.yyubin.application.search.port;

import org.yyubin.domain.search.TrendingKeyword;

import java.time.Duration;
import java.util.List;

public interface SearchKeywordTrendingPort {
    /**
     * Increment keyword score in Redis Sorted Set
     */
    void incrementKeywordScore(String normalizedKeyword, double score, Duration ttl);

    /**
     * Get top N trending keywords from Redis
     */
    List<TrendingKeyword> getTopKeywords(int limit);

    /**
     * Get top N trending keywords for a specific time window
     */
    List<TrendingKeyword> getTopKeywordsByWindow(TimeWindow window, int limit);

    /**
     * Clear trending data (for testing or maintenance)
     */
    void clear(TimeWindow window);

    enum TimeWindow {
        HOURLY,
        DAILY,
        WEEKLY
    }
}
