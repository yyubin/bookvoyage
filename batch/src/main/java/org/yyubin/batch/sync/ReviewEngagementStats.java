package org.yyubin.batch.sync;

public record ReviewEngagementStats(
        long impressions,
        long reaches,
        long clicks,
        long totalDwellMs,
        long dwellCount
) {
    public float ctr() {
        if (impressions <= 0) return 0f;
        return (float) clicks / (float) impressions;
    }

    public float reachRate() {
        if (impressions <= 0) return 0f;
        return (float) reaches / (float) impressions;
    }

    public long avgDwellMs() {
        if (dwellCount <= 0) return 0L;
        return totalDwellMs / dwellCount;
    }
}
