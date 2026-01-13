package org.yyubin.application.recommendation.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.application.recommendation.port.out.AiUserAnalysisPort;
import org.yyubin.application.recommendation.port.out.LLMPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.application.recommendation.service.UserAnalysisPersistenceService;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.ai.AiPromptVersion;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.ai.AiUserAnalysisRecord;
import org.yyubin.domain.ai.AiUserAnalysisRecommendation;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.recommendation.UserAnalysis;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final AiUserAnalysisPort analysisPort;
    private final UserAnalysisContextPort userAnalysisContextPort;
    private final UserAnalysisPersistenceService persistenceService;
    private final SearchBooksUseCase searchBooksUseCase;
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

        // 3. 캐시 키 생성 (날짜 기반 - 하루 1회 분석)
        String cacheKey = buildCacheKey(user);

        // 4. SemanticCache 확인
        return cachePort.get(cacheKey, "user_analysis")
            .map(response -> parseAnalysisFromJson(user.id().value(), response))
            .orElseGet(() -> loadFromDatabaseOrAnalyze(user, context, cacheKey));
    }

    private UserAnalysis loadFromDatabaseOrAnalyze(
        User user,
        UserAnalysisContextPort.UserAnalysisContext context,
        String cacheKey
    ) {
        Optional<AiUserAnalysisRecord> latest = analysisPort.findLatestByUserId(user.id().value());
        if (latest.isPresent()
            && latest.get().status() == AiResultStatus.SUCCESS
            && isSameDay(latest.get().generatedAt(), LocalDate.now())) {
            UserAnalysis analysis = toUserAnalysis(latest.get());
            String cachePayload = serializeAnalysis(analysis);
            if (!cachePayload.isBlank()) {
                cachePort.put(cacheKey, cachePayload, "user_analysis");
            }
            return analysis;
        }
        return analyzeWithLLM(user, context, cacheKey);
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

        // 파싱
        UserAnalysis analysis = parseAnalysisFromJson(user.id().value(), response);
        analysis = validateRecommendations(analysis);
        String cachePayload = serializeAnalysis(analysis);
        if (!cachePayload.isBlank()) {
            cachePort.put(cacheKey, cachePayload, "user_analysis");
        }
        persistAnalysis(user.id().value(), response, analysis, cacheKey);
        return analysis;
    }

    private String buildCacheKey(User user) {
        return String.format("user_analysis_%s_%s", user.id().value(), LocalDate.now());
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

            추천 도서는 실제로 존재하는 책만 포함하세요.
            확실하지 않은 경우 recommendations를 빈 배열로 반환하세요.
            """,
            user.id().value(),
            user.nickname(),
            contextJson
        );
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

    private UserAnalysis validateRecommendations(UserAnalysis analysis) {
        if (analysis.recommendations() == null || analysis.recommendations().isEmpty()) {
            return analysis;
        }

        List<UserAnalysis.BookRecommendation> validated = new ArrayList<>();
        for (UserAnalysis.BookRecommendation rec : analysis.recommendations()) {
            String query = buildRecommendationQuery(rec);
            if (query.isBlank()) {
                continue;
            }
            try {
                BookSearchPage page = searchBooksUseCase.query(
                    new SearchBooksQuery(query, 0, 5, null, SearchOrder.RELEVANCE, PrintType.ALL)
                );
                if (page.items().isEmpty()) {
                    continue;
                }
                BookSearchItem item = page.items().get(0);
                String author = item.getAuthors().isEmpty() ? rec.author() : String.join(", ", item.getAuthors());
                validated.add(UserAnalysis.BookRecommendation.of(item.getTitle(), author, rec.reason()));
            } catch (Exception e) {
                log.warn("Failed to validate recommendation [{}] via external search", query, e);
            }
        }

        return new UserAnalysis(
            analysis.userId(),
            analysis.personaType(),
            analysis.summary(),
            analysis.keywords(),
            validated,
            analysis.analyzedAt()
        );
    }

    private String buildRecommendationQuery(UserAnalysis.BookRecommendation rec) {
        String title = rec.bookTitle() == null ? "" : rec.bookTitle().trim();
        String author = rec.author() == null ? "" : rec.author().trim();
        if (title.isEmpty() && author.isEmpty()) {
            return "";
        }
        if (author.isEmpty()) {
            return title;
        }
        if (title.isEmpty()) {
            return author;
        }
        return title + " " + author;
    }

    private boolean isSameDay(LocalDateTime left, LocalDate right) {
        if (left == null || right == null) {
            return false;
        }
        return left.toLocalDate().isEqual(right);
    }

    private UserAnalysis toUserAnalysis(AiUserAnalysisRecord record) {
        List<UserAnalysis.BookRecommendation> recommendations = new ArrayList<>();
        if (record.recommendations() != null) {
            for (AiUserAnalysisRecommendation rec : record.recommendations()) {
                recommendations.add(UserAnalysis.BookRecommendation.of(
                    rec.bookTitle(),
                    rec.author(),
                    rec.reason()
                ));
            }
        }
        return new UserAnalysis(
            record.userId(),
            record.personaType(),
            record.summary(),
            record.keywords(),
            recommendations,
            record.generatedAt()
        );
    }

    private String serializeAnalysis(UserAnalysis analysis) {
        if (analysis == null) {
            return "";
        }
        try {
            List<UserAnalysis.BookRecommendation> recs =
                analysis.recommendations() == null ? List.of() : analysis.recommendations();
            var payload = java.util.Map.of(
                "persona_type", analysis.personaType(),
                "summary", analysis.summary(),
                "keywords", analysis.keywords(),
                "recommendations", recs.stream()
                    .map(rec -> java.util.Map.of(
                        "book_title", rec.bookTitle(),
                        "author", rec.author(),
                        "reason", rec.reason()
                    ))
                    .toList()
            );
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Failed to serialize analysis for cache", e);
            return "";
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
