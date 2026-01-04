package org.yyubin.domain.ai;

import java.time.LocalDateTime;
import java.util.List;

public record AiUserAnalysisRecord(
    Long id,
    Long userId,
    Long promptVersionId,
    String cacheKey,
    String personaType,
    String summary,
    List<String> keywords,
    String rawResponseJson,
    LocalDateTime generatedAt,
    LocalDateTime expiresAt,
    AiResultStatus status,
    String errorMessage,
    List<AiUserAnalysisRecommendation> recommendations
) {
    public static AiUserAnalysisRecord of(
        Long id,
        Long userId,
        Long promptVersionId,
        String cacheKey,
        String personaType,
        String summary,
        List<String> keywords,
        String rawResponseJson,
        LocalDateTime generatedAt,
        LocalDateTime expiresAt,
        AiResultStatus status,
        String errorMessage,
        List<AiUserAnalysisRecommendation> recommendations
    ) {
        return new AiUserAnalysisRecord(
            id,
            userId,
            promptVersionId,
            cacheKey,
            personaType,
            summary,
            keywords,
            rawResponseJson,
            generatedAt,
            expiresAt,
            status,
            errorMessage,
            recommendations
        );
    }
}
