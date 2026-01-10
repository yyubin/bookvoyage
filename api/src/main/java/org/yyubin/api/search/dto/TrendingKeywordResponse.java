package org.yyubin.api.search.dto;

import org.yyubin.domain.search.TrendingKeyword;

import java.util.List;

public record TrendingKeywordResponse(
    List<KeywordItem> keywords,
    String period,
    int totalCount
) {
    public static TrendingKeywordResponse from(List<TrendingKeyword> keywords, String period) {
        return new TrendingKeywordResponse(
            keywords.stream()
                .map(KeywordItem::from)
                .toList(),
            period,
            keywords.size()
        );
    }

    public record KeywordItem(
        String keyword,
        long score,
        int rank,
        String trend
    ) {
        public static KeywordItem from(TrendingKeyword trendingKeyword) {
            return new KeywordItem(
                trendingKeyword.keyword(),
                trendingKeyword.score(),
                trendingKeyword.rank(),
                trendingKeyword.trend().name().toLowerCase()
            );
        }
    }
}
