package org.yyubin.application.recommendation.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.AiCommunityTrendPort;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.application.recommendation.port.out.LLMPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
import org.yyubin.domain.ai.AiCommunityTrendRecord;
import org.yyubin.domain.ai.AiPromptVersion;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.recommendation.CommunityTrend;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * 커뮤니티 트렌드 분석 Use Case
 * 현재 독서 커뮤니티의 전반적인 경향성을 분석합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzeCommunityTrendUseCase {

    private final SemanticCachePort cachePort;
    private final LLMPort llmPort;
    private final AiCommunityTrendPort trendPort;
    private final AiPromptPort promptPort;
    private final ObjectMapper objectMapper;

    public CommunityTrend execute() {
        // 1. 캐시 키 생성 (날짜 기반 - 하루에 한 번만 분석)
        String cacheKey = buildCacheKey();

        // 2. SemanticCache 확인
        return cachePort.get(cacheKey, "community_trend")
            .map(this::parseTrendFromJson)
            .orElseGet(() -> analyzeWithLLM(cacheKey));
    }

    private CommunityTrend analyzeWithLLM(String cacheKey) {
        log.info("Cache MISS - Analyzing community trend with LLM");

        // LLM 프롬프트 생성
        String prompt = buildPrompt();

        // LLM 호출
        String response = llmPort.complete(prompt, 1000);

        // 캐싱 (24시간)
        cachePort.put(cacheKey, response, "community_trend");

        // 파싱
        CommunityTrend trend = parseTrendFromJson(response);
        persistTrend(response, trend);
        return trend;
    }

    private String buildCacheKey() {
        // 날짜 기반 캐시 키 (하루에 한 번만 분석)
        return String.format("community_trend_%s", LocalDate.now());
    }

    private String buildPrompt() {
        // TODO: 실제 리뷰/북마크 데이터를 기반으로 프롬프트 생성
        return """
            당신은 독서 커뮤니티 트렌드 분석 전문가입니다.

            최근 독서 커뮤니티의 전반적인 분위기와 경향성을 분석하여
            다음 JSON 형식으로 응답하세요:
            {
              "keywords": ["키워드1", "키워드2", "키워드3"],
              "summary": "현재 커뮤니티 분위기에 대한 한 문장 요약",
              "genres": [
                {
                  "genre": "장르명",
                  "percentage": 35.5,
                  "mood": "상승세"
                }
              ]
            }

            mood는 "상승세", "하락세", "안정" 중 하나입니다.
            """;
    }

    private CommunityTrend parseTrendFromJson(String json) {
        try {
            // JSON에서 ``` 제거
            String cleanJson = json;
            if (json.contains("```json")) {
                cleanJson = json.substring(
                    json.indexOf("```json") + 7,
                    json.lastIndexOf("```")
                ).trim();
            }

            JsonNode root = objectMapper.readTree(cleanJson);

            // Keywords 파싱
            List<String> keywords = new ArrayList<>();
            JsonNode keywordsNode = root.get("keywords");
            if (keywordsNode != null && keywordsNode.isArray()) {
                keywordsNode.forEach(node -> keywords.add(node.asText()));
            }

            // Summary 파싱
            String summary = root.has("summary")
                ? root.get("summary").asText()
                : "커뮤니티 트렌드 정보가 없습니다.";

            // Genres 파싱
            List<CommunityTrend.TrendingGenre> genres = new ArrayList<>();
            JsonNode genresNode = root.get("genres");
            if (genresNode != null && genresNode.isArray()) {
                genresNode.forEach(node -> {
                    genres.add(new CommunityTrend.TrendingGenre(
                        node.get("genre").asText(),
                        node.get("percentage").asDouble(),
                        node.get("mood").asText()
                    ));
                });
            }

            return CommunityTrend.of(keywords, summary, genres);

        } catch (Exception e) {
            log.error("Failed to parse LLM response", e);
            throw new RuntimeException("Failed to parse community trend result", e);
        }
    }

    private void persistTrend(String rawResponse, CommunityTrend trend) {
        AiPromptVersion promptVersion = promptPort
            .findActiveVersionByPromptKey("community_trend")
            .orElse(null);

        if (promptVersion == null) {
            log.warn("No active prompt version for community_trend - skipping persistence");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = LocalDate.now().atStartOfDay();
        LocalDateTime windowEnd = LocalDate.now().plusDays(1).atStartOfDay();

        AiCommunityTrendRecord record = AiCommunityTrendRecord.of(
            null,
            promptVersion.id(),
            windowStart,
            windowEnd,
            trend.keywords(),
            trend.summary(),
            trend.genres().stream()
                .map(g -> org.yyubin.domain.ai.AiCommunityTrendGenre.of(
                    g.genre(),
                    g.percentage(),
                    g.mood()
                ))
                .collect(java.util.stream.Collectors.toList()),
            rawResponse,
            now,
            now.plusHours(24),
            AiResultStatus.SUCCESS,
            null
        );

        trendPort.save(record);
    }
}
