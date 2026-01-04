package org.yyubin.domain.recommendation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 커뮤니티 독서 트렌드 분석 결과
 * Value Object - 불변 객체
 */
public record CommunityTrend(
    List<String> keywords,        // ["우울", "힐링", "성장"]
    String summary,               // "요즘 커뮤니티는 힐링과 위로를 주는 책을 선호합니다"
    List<TrendingGenre> genres,   // 장르별 트렌드
    LocalDateTime analyzedAt
) {

    public static CommunityTrend of(
        List<String> keywords,
        String summary,
        List<TrendingGenre> genres
    ) {
        return new CommunityTrend(
            keywords,
            summary,
            genres,
            LocalDateTime.now()
        );
    }

    /**
     * 트렌딩 장르
     */
    public record TrendingGenre(
        String genre,
        Double percentage,  // 전체 대비 비율 (0.0 ~ 1.0)
        String mood         // "상승세", "하락세", "안정"
    ) {
        public static TrendingGenre of(String genre, Double percentage, String mood) {
            return new TrendingGenre(genre, percentage, mood);
        }
    }
}
