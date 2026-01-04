package org.yyubin.domain.ai;

import java.time.LocalDateTime;

public record AiPromptVersion(
    Long id,
    Long promptId,
    int version,
    String template,
    String inputSchemaJson,
    String outputSchemaJson,
    String model,
    double temperature,
    int maxTokens,
    String provider,
    boolean active,
    String createdBy,
    LocalDateTime createdAt
) {
    public static AiPromptVersion of(
        Long id,
        Long promptId,
        int version,
        String template,
        String inputSchemaJson,
        String outputSchemaJson,
        String model,
        double temperature,
        int maxTokens,
        String provider,
        boolean active,
        String createdBy,
        LocalDateTime createdAt
    ) {
        return new AiPromptVersion(
            id,
            promptId,
            version,
            template,
            inputSchemaJson,
            outputSchemaJson,
            model,
            temperature,
            maxTokens,
            provider,
            active,
            createdBy,
            createdAt
        );
    }
}
