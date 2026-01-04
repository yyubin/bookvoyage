package org.yyubin.domain.ai;

import java.time.LocalDateTime;

public record AiPrompt(
    Long id,
    String promptKey,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AiPrompt of(
        Long id,
        String promptKey,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new AiPrompt(
            id,
            promptKey,
            description,
            active,
            createdAt,
            updatedAt
        );
    }
}
