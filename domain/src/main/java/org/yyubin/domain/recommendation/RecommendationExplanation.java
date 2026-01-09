package org.yyubin.domain.recommendation;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 추천 이유 설명
 * Value Object - 불변 객체
 */
public record RecommendationExplanation(
    Long userId,
    Long bookId,
    String explanation,           // "당신이 최근 읽은 판타지 소설들과 비슷한 분위기입니다..."
    Map<String, String> reasons,  // {"그래프": "비슷한 취향...", "시맨틱": "내용 유사..."}
    LocalDateTime createdAt
) {

    public static RecommendationExplanation of(
        Long userId,
        Long bookId,
        String explanation,
        Map<String, String> reasons
    ) {
        return new RecommendationExplanation(
            userId,
            bookId,
            explanation,
            reasons,
            LocalDateTime.now()
        );
    }

    /**
     * 특정 스코어링 방식에 대한 이유 조회
     */
    public String getReasonFor(String scoreType) {
        return reasons.getOrDefault(scoreType, "");
    }

    /**
     * 설명이 있는지 확인
     */
    public boolean hasExplanation() {
        return explanation != null && !explanation.isBlank();
    }
}
