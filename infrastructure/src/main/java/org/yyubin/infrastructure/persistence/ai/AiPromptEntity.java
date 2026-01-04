package org.yyubin.infrastructure.persistence.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.ai.AiPrompt;

@Entity
@Table(
    name = "ai_prompt",
    indexes = {
        @Index(name = "idx_ai_prompt_key", columnList = "prompt_key"),
        @Index(name = "idx_ai_prompt_active", columnList = "is_active")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiPromptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prompt_key", nullable = false, length = 100)
    private String promptKey;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public AiPrompt toDomain() {
        return AiPrompt.of(
            id,
            promptKey,
            description,
            active,
            createdAt,
            updatedAt
        );
    }

    public static AiPromptEntity fromDomain(AiPrompt prompt) {
        return AiPromptEntity.builder()
            .id(prompt.id())
            .promptKey(prompt.promptKey())
            .description(prompt.description())
            .active(prompt.active())
            .createdAt(prompt.createdAt())
            .updatedAt(prompt.updatedAt())
            .build();
    }
}
