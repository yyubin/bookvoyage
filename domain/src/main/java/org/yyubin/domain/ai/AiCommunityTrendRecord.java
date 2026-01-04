package org.yyubin.domain.ai;

import java.time.LocalDateTime;
import java.util.List;

public record AiCommunityTrendRecord(
    Long id,
    Long promptVersionId,
    LocalDateTime windowStart,
    LocalDateTime windowEnd,
    List<String> keywords,
    String summary,
    List<AiCommunityTrendGenre> genres,
    String rawResponseJson,
    LocalDateTime generatedAt,
    LocalDateTime expiresAt,
    AiResultStatus status,
    String errorMessage
) {
    public static AiCommunityTrendRecord of(
        Long id,
        Long promptVersionId,
        LocalDateTime windowStart,
        LocalDateTime windowEnd,
        List<String> keywords,
        String summary,
        List<AiCommunityTrendGenre> genres,
        String rawResponseJson,
        LocalDateTime generatedAt,
        LocalDateTime expiresAt,
        AiResultStatus status,
        String errorMessage
    ) {
        return new AiCommunityTrendRecord(
            id,
            promptVersionId,
            windowStart,
            windowEnd,
            keywords,
            summary,
            genres,
            rawResponseJson,
            generatedAt,
            expiresAt,
            status,
            errorMessage
        );
    }
}
