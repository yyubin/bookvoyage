package org.yyubin.application.recommendation.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.application.recommendation.port.out.LLMPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.application.recommendation.service.UserAnalysisPersistenceService;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.ai.AiPromptVersion;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.ai.AiUserAnalysisRecord;
import org.yyubin.domain.ai.AiUserAnalysisRecommendation;
import org.yyubin.domain.recommendation.UserAnalysis;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 사용자 독서 취향 분석 Use Case
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(
    prefix = "ai.enrichment",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class AnalyzeUserPreferenceUseCase {

    private final LoadUserPort loadUserPort;
    private final SemanticCachePort cachePort;
    private final LLMPort llmPort;
    private final AiPromptPort promptPort;
    private final UserAnalysisContextPort userAnalysisContextPort;
    private final UserAnalysisPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    private static final int RECENT_REVIEW_LIMIT = 8;
    private static final int RECENT_LIBRARY_LIMIT = 8;
    private static final int RECENT_SEARCH_LIMIT = 10;
    private static final int RECENT_SEARCH_DAYS = 30;

    public UserAnalysis execute(Long userId) {
        // 1. 사용자 정보 조회
        User user = loadUserPort.loadById(new UserId(userId));

        // 2. 사용자 컨텍스트 수집
        UserAnalysisContextPort.UserAnalysisContext context = userAnalysisContextPort.loadContext(
            userId,
            RECENT_REVIEW_LIMIT,
            RECENT_LIBRARY_LIMIT,
            RECENT_SEARCH_LIMIT,
            LocalDateTime.now().minusDays(RECENT_SEARCH_DAYS)
        );

        // 3. 캐시 키 생성 (최근 활동 기반)
        String cacheKey = buildCacheKey(user, context);

        // 4. SemanticCache 확인
        return cachePort.get(cacheKey, "user_analysis")
            .map(response -> parseAnalysisFromJson(user.id().value(), response))
            .orElseGet(() -> analyzeWithLLM(user, context, cacheKey));
    }

    private UserAnalysis analyzeWithLLM(
        User user,
        UserAnalysisContextPort.UserAnalysisContext context,
        String cacheKey
    ) {
        log.info("Cache MISS - Analyzing user {} with LLM", user.id());

        // LLM 프롬프트 생성
        String prompt = buildPrompt(user, context);

        // LLM 호출
        String response = llmPort.complete(prompt, 800);

        // 캐싱
        cachePort.put(cacheKey, response, "user_analysis");

        // 파싱
        UserAnalysis analysis = parseAnalysisFromJson(user.id().value(), response);
        persistAnalysis(user.id().value(), response, analysis, cacheKey);
        return analysis;
    }

    private String buildCacheKey(User user, UserAnalysisContextPort.UserAnalysisContext context) {
        String signature = buildContextSignature(context);
        return String.format("user_analysis_%s_%s", user.id().value(), signature);
    }

    private String buildPrompt(User user, UserAnalysisContextPort.UserAnalysisContext context) {
        String contextJson;
        try {
            contextJson = objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            log.warn("Failed to serialize user analysis context, falling back to empty context", e);
            contextJson = "{}";
        }

        return String.format("""
            당신은 독서 취향 분석 전문가입니다.

            사용자 정보:
            - 사용자 ID: %s
            - 닉네임: %s

            아래 JSON은 사용자의 최근 활동 요약입니다. 이를 근거로 분석하세요:
            %s

            이 사용자의 독서 성향을 분석하고 다음 JSON 형식으로 응답하세요:
            {
              "persona_type": "성향을 나타내는 영문 키워드",
              "summary": "한 문장 요약",
              "keywords": ["키워드1", "키워드2", "키워드3"],
              "recommendations": [
                {
                  "book_title": "책 제목",
                  "author": "저자",
                  "reason": "추천 이유"
                }
              ]
            }
            """,
            user.id().value(),
            user.nickname(),
            contextJson
        );
    }

    private String buildContextSignature(UserAnalysisContextPort.UserAnalysisContext context) {
        if (context == null) {
            return "none";
        }

        LocalDateTime latestReviewAt = context.recentReviews().stream()
            .map(UserAnalysisContextPort.ReviewSnapshot::createdAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        LocalDateTime latestLibraryAt = context.recentLibraryUpdates().stream()
            .map(UserAnalysisContextPort.UserBookSnapshot::updatedAt)
            .filter(Objects::nonNull)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        String searchHash = String.valueOf(context.recentSearchQueries().hashCode());
        String combined = String.format(
            "%s|%s|%s|%s|%s",
            context.recentReviews().size(),
            latestReviewAt,
            context.recentLibraryUpdates().size(),
            latestLibraryAt,
            searchHash
        );

        return Integer.toHexString(combined.hashCode());
    }

    private UserAnalysis parseAnalysisFromJson(Long userId, String json) {
        try {
            String cleanJson = json;
            if (json.contains("```")) {
                int start = json.indexOf("```");
                int end = json.lastIndexOf("```");
                if (end > start) {
                    cleanJson = json.substring(start + 3, end).trim();
                    if (cleanJson.startsWith("json")) {
                        cleanJson = cleanJson.substring(4).trim();
                    }
                }
            }

            int firstBrace = cleanJson.indexOf('{');
            int lastBrace = cleanJson.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                cleanJson = cleanJson.substring(firstBrace, lastBrace + 1);
            }

            var root = objectMapper.readTree(cleanJson);

            String personaType = root.hasNonNull("persona_type")
                ? root.get("persona_type").asText()
                : "general_reader";
            String summary = root.hasNonNull("summary")
                ? root.get("summary").asText()
                : "독서를 즐기는 사용자입니다";

            List<String> keywords = new ArrayList<>();
            var keywordsNode = root.get("keywords");
            if (keywordsNode != null && keywordsNode.isArray()) {
                keywordsNode.forEach(node -> keywords.add(node.asText()));
            }

            List<UserAnalysis.BookRecommendation> recommendations = new ArrayList<>();
            var recsNode = root.get("recommendations");
            if (recsNode != null && recsNode.isArray()) {
                recsNode.forEach(node -> {
                    String title = node.hasNonNull("book_title") ? node.get("book_title").asText() : "";
                    String author = node.hasNonNull("author") ? node.get("author").asText() : "";
                    String reason = node.hasNonNull("reason") ? node.get("reason").asText() : "";
                    recommendations.add(UserAnalysis.BookRecommendation.of(title, author, reason));
                });
            }

            return UserAnalysis.of(
                userId,
                personaType,
                summary,
                keywords,
                recommendations
            );

        } catch (Exception e) {
            log.error("Failed to parse LLM response", e);
            throw new RuntimeException("Failed to parse user analysis result", e);
        }
    }

    private void persistAnalysis(
        Long userId,
        String rawResponse,
        UserAnalysis analysis,
        String cacheKey
    ) {
        AiPromptVersion promptVersion = promptPort
            .findActiveVersionByPromptKey("user_analysis")
            .orElse(null);

        if (promptVersion == null) {
            log.warn("No active prompt version for user_analysis - skipping persistence");
            return;
        }

        List<AiUserAnalysisRecommendation> recommendations = new ArrayList<>();
        if (analysis.recommendations() != null) {
            int rank = 1;
            for (UserAnalysis.BookRecommendation rec : analysis.recommendations()) {
                recommendations.add(AiUserAnalysisRecommendation.of(
                    null,
                    null,
                    null,
                    rec.bookTitle(),
                    rec.author(),
                    rec.reason(),
                    rank++
                ));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        AiUserAnalysisRecord record = AiUserAnalysisRecord.of(
            null,
            userId,
            promptVersion.id(),
            cacheKey,
            analysis.personaType(),
            analysis.summary(),
            analysis.keywords(),
            rawResponse,
            now,
            now.plusHours(24),
            AiResultStatus.SUCCESS,
            null,
            recommendations
        );

        persistenceService.save(record);
    }
}
