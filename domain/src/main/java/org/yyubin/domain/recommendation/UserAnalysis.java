package org.yyubin.domain.recommendation;

import java.time.LocalDateTime;
import java.util.List;

public record UserAnalysis(
    Long userId,
    String personaType,      // "fantasy_enthusiast", "scifi_lover" 등
    String summary,           // "판타지 장르를 선호하며 모험과 성장 스토리를 좋아합니다"
    List<String> keywords,    // ["모험", "마법", "성장"]
    List<BookRecommendation> recommendations,
    LocalDateTime analyzedAt
) {

    public static UserAnalysis of(
        Long userId,
        String personaType,
        String summary,
        List<String> keywords,
        List<BookRecommendation> recommendations
    ) {
        return new UserAnalysis(
            userId,
            personaType,
            summary,
            keywords,
            recommendations,
            LocalDateTime.now()
        );
    }

    /**
     * AI 추천 도서
     */
    public record BookRecommendation(
        String bookTitle,
        String author,
        String reason
    ) {
        public static BookRecommendation of(
            String bookTitle,
            String author,
            String reason
        ) {
            return new BookRecommendation(bookTitle, author, reason);
        }
    }
}
