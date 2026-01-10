package org.yyubin.application.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.yyubin.application.recommendation.usecase.AnalyzeCommunityTrendUseCase;
import org.yyubin.application.search.port.SearchKeywordTrendingPort;
import org.yyubin.application.search.port.SearchQueryQueuePort;
import org.yyubin.domain.search.SearchQuery;
import org.yyubin.domain.search.TrendingKeyword.TrendDirection;
import org.yyubin.domain.search.TrendingKeyword;
import org.yyubin.domain.recommendation.CommunityTrend;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKeywordTrendingService {

    private final SearchKeywordTrendingPort trendingPort;
    private final SearchQueryQueuePort queuePort;
    private final ObjectProvider<AnalyzeCommunityTrendUseCase> communityTrendUseCaseProvider;

    /**
     * Log a search query and update trending keywords in real-time
     */
    public void logSearchQuery(SearchQuery searchQuery) {
        // Enqueue for batch insert to MySQL
        queuePort.enqueue(searchQuery);

        // Update Redis trending immediately with weighted score
        double score = calculateScore(searchQuery);
        trendingPort.incrementKeywordScore(
            searchQuery.normalizedQuery(),
            score,
            Duration.ofDays(1)
        );

        log.debug("Logged search query: '{}' with score: {}", searchQuery.normalizedQuery(), score);
    }

    /**
     * Get trending keywords
     */
    public List<TrendingKeyword> getTrendingKeywords(int limit) {
        return trendingPort.getTopKeywords(limit);
    }

    /**
     * Get trending keywords by time window
     */
    public List<TrendingKeyword> getTrendingKeywordsByWindow(
        SearchKeywordTrendingPort.TimeWindow window,
        int limit
    ) {
        List<TrendingKeyword> keywords = trendingPort.getTopKeywordsByWindow(window, limit);
        if (!keywords.isEmpty()) {
            return keywords;
        }
        return fallbackFromCommunityTrend(limit);
    }

    /**
     * Calculate weighted score for trending algorithm
     *
     * Factors:
     * - Time decay: Recent searches get higher scores
     * - Result count boost: Searches with results are weighted higher
     *
     * Base score = 1.0
     * Time decay multiplier:
     * - Last 1 hour: 1.5x
     * - Last 3 hours: 1.2x
     * - Older: 1.0x
     *
     * Result boost:
     * - Has results (> 0): 1.2x
     * - No results: 1.0x
     */
    private double calculateScore(SearchQuery searchQuery) {
        double baseScore = 1.0;

        // Time decay
        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = ChronoUnit.MINUTES.between(searchQuery.createdAt(), now);
        double timeDecay;
        if (minutesAgo <= 60) {
            timeDecay = 1.5;
        } else if (minutesAgo <= 180) {
            timeDecay = 1.2;
        } else {
            timeDecay = 1.0;
        }

        // Result count boost
        double resultBoost = (searchQuery.resultCount() != null && searchQuery.resultCount() > 0) ? 1.2 : 1.0;

        return baseScore * timeDecay * resultBoost;
    }

    private List<TrendingKeyword> fallbackFromCommunityTrend(int limit) {
        AnalyzeCommunityTrendUseCase useCase = communityTrendUseCaseProvider.getIfAvailable();
        if (useCase == null) {
            return List.of();
        }

        try {
            return useCase.findCachedTrend()
                .map(trend -> toTrendingKeywords(trend, limit))
                .orElse(List.of());
        } catch (Exception e) {
            log.debug("Failed to load community trend for search keyword fallback", e);
            return List.of();
        }
    }

    private List<TrendingKeyword> toTrendingKeywords(CommunityTrend trend, int limit) {
        if (trend == null || trend.keywords() == null || trend.keywords().isEmpty()) {
            return List.of();
        }

        int size = Math.min(limit, trend.keywords().size());
        List<TrendingKeyword> results = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < size; i++) {
            String keyword = trend.keywords().get(i);
            if (keyword == null) {
                continue;
            }
            String trimmed = keyword.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            results.add(new TrendingKeyword(trimmed, 0L, rank++, TrendDirection.NEW));
        }
        return results;
    }
}
