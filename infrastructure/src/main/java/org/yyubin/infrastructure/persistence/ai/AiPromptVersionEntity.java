package org.yyubin.infrastructure.persistence.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.ai.AiPromptVersion;

@Entity
@Table(
    name = "ai_prompt_version",
    indexes = {
        @Index(name = "idx_ai_prompt_version_prompt", columnList = "prompt_id"),
        @Index(name = "idx_ai_prompt_version_active", columnList = "prompt_id, is_active")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiPromptVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prompt_id", nullable = false, insertable = false, updatable = false)
    private AiPromptEntity prompt;

    @Column(name = "prompt_id", nullable = false)
    private Long promptId;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;

    @Column(name = "input_schema", columnDefinition = "JSON")
    private String inputSchemaJson;

    @Column(name = "output_schema", columnDefinition = "JSON")
    private String outputSchemaJson;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "temperature", nullable = false)
    private double temperature;

    @Column(name = "max_tokens", nullable = false)
    private int maxTokens;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AiPromptVersion toDomain() {
        return AiPromptVersion.of(
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

    public static AiPromptVersionEntity fromDomain(AiPromptVersion version) {
        return AiPromptVersionEntity.builder()
            .id(version.id())
            .promptId(version.promptId())
            .version(version.version())
            .template(version.template())
            .inputSchemaJson(version.inputSchemaJson())
            .outputSchemaJson(version.outputSchemaJson())
            .model(version.model())
            .temperature(version.temperature())
            .maxTokens(version.maxTokens())
            .provider(version.provider())
            .active(version.active())
            .createdBy(version.createdBy())
            .createdAt(version.createdAt())
            .build();
    }
}
