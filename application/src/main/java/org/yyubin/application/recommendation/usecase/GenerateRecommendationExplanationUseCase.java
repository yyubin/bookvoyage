package org.yyubin.application.recommendation.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.application.recommendation.port.out.AiRecommendationExplanationPort;
import org.yyubin.application.recommendation.port.out.LLMPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.ai.AiPromptVersion;
import org.yyubin.domain.ai.AiRecommendationExplanationRecord;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.recommendation.RecommendationExplanation;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenerateRecommendationExplanationUseCase {

    private final LoadUserPort loadUserPort;
    private final SemanticCachePort cachePort;
    private final LLMPort llmPort;
    private final AiRecommendationExplanationPort explanationPort;
    private final AiPromptPort promptPort;
    private final ObjectMapper objectMapper;

    /**
     * 추천 설명 생성
     *
     * @param userId 사용자 ID
     * @param bookId 책 ID
     * @param bookTitle 책 제목
     * @param scoreDetails 각 스코어링 방식별 점수 (예: {"그래프": "8.5", "시맨틱": "7.2"})
     * @return 추천 이유 설명
     */
    public RecommendationExplanation execute(
        Long userId,
        Long bookId,
        String bookTitle,
        Map<String, String> scoreDetails
    ) {
        // 1. 사용자 정보 조회
        User user = loadUserPort.loadById(new UserId(userId));

        // 2. 캐시 키 생성
        String cacheKey = buildCacheKey(userId, bookId);

        // 3. SemanticCache 확인
        String explanation = cachePort.get(cacheKey, "recommendation_explanation")
            .orElseGet(() -> generateWithLLM(user, bookTitle, scoreDetails, cacheKey));

        // 4. RecommendationExplanation 생성
        Map<String, String> reasons = parseReasonsFromExplanation(explanation, scoreDetails);

        return RecommendationExplanation.of(
            userId,
            bookId,
            explanation,
            reasons
        );
    }

    private String generateWithLLM(
        User user,
        String bookTitle,
        Map<String, String> scoreDetails,
        String cacheKey
    ) {
        log.info("Cache MISS - Generating explanation for user {} and book {}",
            user.id(), bookTitle);

        // LLM 프롬프트 생성
        String prompt = buildPrompt(user, bookTitle, scoreDetails);

        // LLM 호출
        String response = llmPort.complete(prompt, 300);

        // 캐싱
        cachePort.put(cacheKey, response, "recommendation_explanation");

        persistExplanation(user.id().value(), bookId, response, scoreDetails);

        return response;
    }

    private String buildCacheKey(Long userId, Long bookId) {
        return String.format("explanation_%d_%d", userId, bookId);
    }

    private String buildPrompt(User user, String bookTitle, Map<String, String> scoreDetails) {
        // TODO: User의 독서 이력, 취향 태그 등을 프롬프트에 포함
        String scoreInfo = scoreDetails.entrySet().stream()
            .map(e -> String.format("%s: %s점", e.getKey(), e.getValue()))
            .reduce((a, b) -> a + ", " + b)
            .orElse("정보 없음");

        return String.format("""
            당신은 친절한 독서 추천 전문가입니다.

            사용자 정보:
            - 닉네임: %s
            - 취향 태그: %s

            추천하는 책:
            - 제목: %s
            - 추천 점수: %s

            이 책을 추천하는 이유를 친절하고 자연스럽게 한두 문장으로 설명해주세요.
            마치 친구에게 책을 추천하는 것처럼 따뜻하고 구체적으로 작성하세요.
            """,
            user.nickname(),
            user.tasteTag() != null && !user.tasteTag().isBlank()
                ? user.tasteTag()
                : "아직 취향 분석 전",
            bookTitle,
            scoreInfo
        );
    }

    private Map<String, String> parseReasonsFromExplanation(
        String explanation,
        Map<String, String> scoreDetails
    ) {
        Map<String, String> reasons = new HashMap<>();

        // 각 스코어링 방식에 대한 간단한 설명 추가
        scoreDetails.forEach((scoreType, score) -> {
            String reason = switch (scoreType) {
                case "그래프" -> "비슷한 취향의 독자들이 함께 읽은 책입니다.";
                case "시맨틱" -> "내용이 당신의 관심사와 유사합니다.";
                case "컨텍스트" -> "최근 읽은 책들과 연관성이 높습니다.";
                default -> "추천 시스템이 선정한 책입니다.";
            };
            reasons.put(scoreType, reason);
        });

        return reasons;
    }

    private void persistExplanation(
        Long userId,
        Long bookId,
        String rawResponse,
        Map<String, String> scoreDetails
    ) {
        AiPromptVersion promptVersion = promptPort
            .findActiveVersionByPromptKey("recommendation_explanation")
            .orElse(null);

        if (promptVersion == null) {
            log.warn("No active prompt version for recommendation_explanation - skipping persistence");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        AiRecommendationExplanationRecord record = AiRecommendationExplanationRecord.of(
            null,
            userId,
            bookId,
            promptVersion.id(),
            rawResponse,
            scoreDetails,
            rawResponse,
            now,
            now.plusHours(24),
            AiResultStatus.SUCCESS,
            null
        );

        explanationPort.save(record);
    }
}
