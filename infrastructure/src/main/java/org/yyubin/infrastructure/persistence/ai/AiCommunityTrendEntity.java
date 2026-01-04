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
import org.yyubin.domain.ai.AiCommunityTrendGenre;
import org.yyubin.domain.ai.AiCommunityTrendRecord;
import org.yyubin.domain.ai.AiResultStatus;

@Entity
@Table(
    name = "ai_community_trend",
    indexes = {
        @Index(name = "idx_ai_community_trend_window", columnList = "window_start, window_end"),
        @Index(name = "idx_ai_community_trend_generated", columnList = "generated_at"),
        @Index(name = "idx_ai_community_trend_prompt", columnList = "prompt_version_id")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiCommunityTrendEntity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prompt_version_id", nullable = false)
    private Long promptVersionId;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "keywords", columnDefinition = "JSON", nullable = false)
    private String keywordsJson;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "genres", columnDefinition = "JSON")
    private String genresJson;

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

    public AiCommunityTrendRecord toDomain() {
        return AiCommunityTrendRecord.of(
            id,
            promptVersionId,
            windowStart,
            windowEnd,
            readKeywords(keywordsJson),
            summary,
            readGenres(genresJson),
            rawResponseJson,
            generatedAt,
            expiresAt,
            status,
            errorMessage
        );
    }

    public static AiCommunityTrendEntity fromDomain(AiCommunityTrendRecord record) {
        return AiCommunityTrendEntity.builder()
            .id(record.id())
            .promptVersionId(record.promptVersionId())
            .windowStart(record.windowStart())
            .windowEnd(record.windowEnd())
            .keywordsJson(writeKeywords(record.keywords()))
            .summary(record.summary())
            .genresJson(writeGenres(record.genres()))
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

    private static String writeGenres(List<AiCommunityTrendGenre> genres) {
        if (genres == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(genres);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize genres", e);
        }
    }

    private static List<AiCommunityTrendGenre> readGenres(String genresJson) {
        if (genresJson == null || genresJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(genresJson, new TypeReference<List<AiCommunityTrendGenre>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize genres", e);
        }
    }
}
