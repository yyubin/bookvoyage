package org.yyubin.application.recommendation.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.application.recommendation.port.out.AiUserAnalysisPort;
import org.yyubin.application.recommendation.port.out.LLMPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
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

/**
 * 사용자 독서 취향 분석 Use Case
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyzeUserPreferenceUseCase {

    private final LoadUserPort loadUserPort;
    private final SemanticCachePort cachePort;
    private final LLMPort llmPort;
    private final AiUserAnalysisPort analysisPort;
    private final AiPromptPort promptPort;
    private final ObjectMapper objectMapper;

    public UserAnalysis execute(Long userId) {
        // 1. 사용자 정보 조회
        User user = loadUserPort.loadById(new UserId(userId));

        // 2. 캐시 키 생성 (사용자 성향 기반)
        String cacheKey = buildCacheKey(user);

        // 3. SemanticCache 확인
        return cachePort.get(cacheKey, "user_analysis")
            .map(this::parseAnalysisFromJson)
            .orElseGet(() -> analyzeWithLLM(user, cacheKey));
    }

    private UserAnalysis analyzeWithLLM(User user, String cacheKey) {
        log.info("Cache MISS - Analyzing user {} with LLM", user.id());

        // LLM 프롬프트 생성
        String prompt = buildPrompt(user);

        // LLM 호출
        String response = llmPort.complete(prompt, 800);

        // 캐싱
        cachePort.put(cacheKey, response, "user_analysis");

        // 파싱
        UserAnalysis analysis = parseAnalysisFromJson(response);
        persistAnalysis(user.id().value(), response, analysis, cacheKey);
        return analysis;
    }

    private String buildCacheKey(User user) {
        // 비슷한 성향의 사용자를 같은 키로 매핑
        // TODO: User 모델에 맞춰 실제 필드 사용
        return String.format("user_analysis_%s", user.id().value());
    }

    private String buildPrompt(User user) {
        // TODO: User 모델의 실제 필드를 사용하도록 수정 필요
        return String.format("""
            당신은 독서 취향 분석 전문가입니다.

            사용자 정보:
            - 사용자 ID: %s
            - 닉네임: %s

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
            user.nickname()
        );
    }

    private UserAnalysis parseAnalysisFromJson(String json) {
        try {
            // JSON에서 ``` 제거
            String cleanJson = json;
            if (json.contains("```json")) {
                cleanJson = json.substring(
                    json.indexOf("```json") + 7,
                    json.lastIndexOf("```")
                ).trim();
            }

            // TODO: 실제 JSON 파싱 구현
            // 임시로 빈 객체 반환
            return UserAnalysis.of(
                1L,
                "general_reader",
                "독서를 즐기는 사용자입니다",
                java.util.List.of("독서", "성장"),
                java.util.List.of()
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

        analysisPort.save(record);
    }
}
