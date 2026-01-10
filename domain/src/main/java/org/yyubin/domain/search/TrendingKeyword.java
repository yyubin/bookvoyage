package org.yyubin.domain.search;

public record TrendingKeyword(
    String keyword,
    long score,
    int rank,
    TrendDirection trend
) {
    public static TrendingKeyword of(String keyword, long score, int rank) {
        return new TrendingKeyword(keyword, score, rank, TrendDirection.STABLE);
    }

    public TrendingKeyword withTrend(TrendDirection trend) {
        return new TrendingKeyword(keyword, score, rank, trend);
    }

    public enum TrendDirection {
        UP,
        DOWN,
        STABLE,
        NEW
    }
}
