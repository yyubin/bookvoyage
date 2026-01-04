package org.yyubin.domain.ai;

import java.time.LocalDateTime;
import java.util.Map;

public record AiRecommendationExplanationRecord(
    Long id,
    Long userId,
    Long bookId,
    Long promptVersionId,
    String explanation,
    Map<String, String> scoreDetails,
    String rawResponseJson,
    LocalDateTime generatedAt,
    LocalDateTime expiresAt,
    AiResultStatus status,
    String errorMessage
) {
    public static AiRecommendationExplanationRecord of(
        Long id,
        Long userId,
        Long bookId,
        Long promptVersionId,
        String explanation,
        Map<String, String> scoreDetails,
        String rawResponseJson,
        LocalDateTime generatedAt,
        LocalDateTime expiresAt,
        AiResultStatus status,
        String errorMessage
    ) {
        return new AiRecommendationExplanationRecord(
            id,
            userId,
            bookId,
            promptVersionId,
            explanation,
            scoreDetails,
            rawResponseJson,
            generatedAt,
            expiresAt,
            status,
            errorMessage
        );
    }
}
