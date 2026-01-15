package org.yyubin.application.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.application.recommendation.port.out.LLMPort;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.domain.recommendation.UserAnalysis;
import org.yyubin.domain.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 기반 사용자 분석 서비스
 * LLM 프롬프트 생성, 호출, 응답 파싱을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalysisLLMService {

    private final LLMPort llmPort;
    private final ObjectMapper objectMapper;

    private static final int LLM_MAX_TOKENS = 800;

    /**
     * LLM 분석 결과를 담는 레코드
     * 원본 응답과 파싱된 분석 결과를 모두 포함
     */
    public record LLMAnalysisResult(
        String rawResponse,
        UserAnalysis analysis
    ) {}

    /**
     * LLM을 사용하여 사용자 취향 분석 수행
     *
     * @param user 분석 대상 사용자
     * @param context 사용자 활동 컨텍스트
     * @return LLM 분석 결과 (원본 응답 + 파싱된 분석)
     */
    public LLMAnalysisResult analyzeWithLLM(User user, UserAnalysisContextPort.UserAnalysisContext context) {
        log.info("Calling LLM for user {} analysis", user.id());
        String prompt = buildPrompt(user, context);
        String rawResponse = llmPort.complete(prompt, LLM_MAX_TOKENS);
        UserAnalysis analysis = parseAnalysisFromJson(user.id().value(), rawResponse);
        return new LLMAnalysisResult(rawResponse, analysis);
    }

    /**
     * JSON 문자열을 UserAnalysis 객체로 파싱
     * CacheService에서도 재사용됨 (public 메서드)
     *
     * @param userId 사용자 ID
     * @param json LLM 응답 또는 캐시된 JSON
     * @return 파싱된 UserAnalysis 객체
     */
    public UserAnalysis parseAnalysisFromJson(Long userId, String json) {
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

    /**
     * LLM 프롬프트 생성
     *
     * @param user 사용자 정보
     * @param context 사용자 활동 컨텍스트
     * @return 생성된 프롬프트
     */
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
}
