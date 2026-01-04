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
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.ai.AiRecommendationExplanationRecord;
import org.yyubin.domain.ai.AiResultStatus;

@Entity
@Table(
    name = "ai_recommendation_explanation",
    indexes = {
        @Index(name = "idx_ai_recommendation_expl_user", columnList = "user_id, generated_at"),
        @Index(name = "idx_ai_recommendation_expl_book", columnList = "book_id"),
        @Index(name = "idx_ai_recommendation_expl_prompt", columnList = "prompt_version_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiRecommendationExplanationEntity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "prompt_version_id", nullable = false)
    private Long promptVersionId;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "score_details", columnDefinition = "JSON")
    private String scoreDetailsJson;

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

    public AiRecommendationExplanationRecord toDomain() {
        return AiRecommendationExplanationRecord.of(
            id,
            userId,
            bookId,
            promptVersionId,
            explanation,
            readScoreDetails(scoreDetailsJson),
            rawResponseJson,
            generatedAt,
            expiresAt,
            status,
            errorMessage
        );
    }

    public static AiRecommendationExplanationEntity fromDomain(AiRecommendationExplanationRecord record) {
        return AiRecommendationExplanationEntity.builder()
            .id(record.id())
            .userId(record.userId())
            .bookId(record.bookId())
            .promptVersionId(record.promptVersionId())
            .explanation(record.explanation())
            .scoreDetailsJson(writeScoreDetails(record.scoreDetails()))
            .rawResponseJson(record.rawResponseJson())
            .generatedAt(record.generatedAt())
            .expiresAt(record.expiresAt())
            .status(record.status())
            .errorMessage(record.errorMessage())
            .build();
    }

    private static String writeScoreDetails(Map<String, String> scoreDetails) {
        if (scoreDetails == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(scoreDetails);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize score details", e);
        }
    }

    private static Map<String, String> readScoreDetails(String scoreDetailsJson) {
        if (scoreDetailsJson == null || scoreDetailsJson.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(scoreDetailsJson, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize score details", e);
        }
    }
}
