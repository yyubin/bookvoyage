package org.yyubin.infrastructure.persistence.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.ai.AiUserAnalysisRecord;
import org.yyubin.domain.ai.AiUserAnalysisRecommendation;

@Entity
@Table(
    name = "ai_user_analysis",
    indexes = {
        @Index(name = "idx_ai_user_analysis_user", columnList = "user_id, generated_at"),
        @Index(name = "idx_ai_user_analysis_prompt", columnList = "prompt_version_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiUserAnalysisEntity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "prompt_version_id", nullable = false)
    private Long promptVersionId;

    @Column(name = "cache_key", length = 255)
    private String cacheKey;

    @Column(name = "persona_type", nullable = false, length = 100)
    private String personaType;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "keywords", columnDefinition = "JSON")
    private String keywordsJson;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponseJson;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AiResultStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public AiUserAnalysisRecord toDomain(List<AiUserAnalysisRecommendation> recommendations) {
        return AiUserAnalysisRecord.of(
            id,
            userId,
            promptVersionId,
            cacheKey,
            personaType,
            summary,
            readKeywords(keywordsJson),
            rawResponseJson,
            generatedAt,
            expiresAt,
            status,
            errorMessage,
            recommendations
        );
    }

    public static AiUserAnalysisEntity fromDomain(AiUserAnalysisRecord record) {
        return AiUserAnalysisEntity.builder()
            .id(record.id())
            .userId(record.userId())
            .promptVersionId(record.promptVersionId())
            .cacheKey(record.cacheKey())
            .personaType(record.personaType())
            .summary(record.summary())
            .keywordsJson(writeKeywords(record.keywords()))
            .rawResponseJson(record.rawResponseJson())
            .generatedAt(record.generatedAt())
            .expiresAt(record.expiresAt())
            .status(record.status())
            .errorMessage(record.errorMessage())
            .build();
    }

    private static String writeKeywords(List<String> keywords) {
        if (keywords == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(keywords);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize keywords", e);
        }
    }

    private static List<String> readKeywords(String keywordsJson) {
        if (keywordsJson == null || keywordsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(keywordsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize keywords", e);
        }
    }
}
