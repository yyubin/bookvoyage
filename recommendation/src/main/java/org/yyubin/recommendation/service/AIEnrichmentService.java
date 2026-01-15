package org.yyubin.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yyubin.application.recommendation.AnalyzeUserPreferenceUseCase;
import org.yyubin.application.recommendation.usecase.GenerateRecommendationExplanationUseCase;
import org.yyubin.domain.recommendation.RecommendationExplanation;
import org.yyubin.domain.recommendation.UserAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIEnrichmentService {

    private final AnalyzeUserPreferenceUseCase analyzeUserPreferenceUseCase;
    private final GenerateRecommendationExplanationUseCase generateRecommendationExplanationUseCase;

    @Value("${ai.enrichment.enabled:false}")
    private boolean enabled;

    /**
     * 사용자 취향 분석
     *
     * @param userId 사용자 ID
     * @return 사용자 분석 결과 (AI disabled이면 empty)
     */
    public Optional<UserAnalysis> analyzeUserPreference(Long userId) {
        if (!enabled) {
            log.debug("AI enrichment is disabled");
            return Optional.empty();
        }

        try {
            UserAnalysis analysis = analyzeUserPreferenceUseCase.execute(userId);
            log.info("User preference analyzed - userId: {}, persona: {}",
                userId, analysis.personaType());
            return Optional.of(analysis);

        } catch (Exception e) {
            log.error("Failed to analyze user preference for user {}", userId, e);
            return Optional.empty(); // AI 실패해도 추천 자체는 정상 동작
        }
    }

    /**
     * 추천 설명 생성
     *
     * @param userId 사용자 ID
     * @param bookId 책 ID
     * @param bookTitle 책 제목
     * @param scoreDetails 스코어 상세 (예: {"그래프": "8.5", "시맨틱": "7.2"})
     * @return 추천 설명 (AI disabled이거나 실패하면 empty)
     */
    public Optional<RecommendationExplanation> generateExplanation(
        Long userId,
        Long bookId,
        String bookTitle,
        Map<String, String> scoreDetails
    ) {
        if (!enabled) {
            return Optional.empty();
        }

        try {
            RecommendationExplanation explanation = generateRecommendationExplanationUseCase.execute(
                userId,
                bookId,
                bookTitle,
                scoreDetails
            );

            log.debug("Recommendation explanation generated - userId: {}, bookId: {}",
                userId, bookId);
            return Optional.of(explanation);

        } catch (Exception e) {
            log.error("Failed to generate explanation for user {} and book {}",
                userId, bookId, e);
            return Optional.empty();
        }
    }

    /**
     * 추천 결과 일괄 설명 생성
     *
     * @param userId 사용자 ID
     * @param results 추천 결과 리스트
     * @return bookId -> 설명 맵
     */
    public Map<Long, RecommendationExplanation> enrichRecommendations(
        Long userId,
        java.util.List<RecommendationResult> results
    ) {
        if (!enabled || results == null || results.isEmpty()) {
            return Map.of();
        }

        Map<Long, RecommendationExplanation> explanations = new HashMap<>();

        // 각 추천 결과에 대해 설명 생성 (비동기 또는 배치 처리 가능)
        for (RecommendationResult result : results) {
            try {
                Map<String, String> scoreDetails = new HashMap<>();
                scoreDetails.put("추천점수", String.format("%.2f", result.getScore()));
                if (result.getSource() != null) {
                    scoreDetails.put("출처", result.getSource());
                }

                // 책 제목은 별도로 조회 필요, 우선은 bookId로 대체
                String bookTitle = "Book #" + result.getBookId();

                Optional<RecommendationExplanation> explanation = generateExplanation(
                    userId,
                    result.getBookId(),
                    bookTitle,
                    scoreDetails
                );

                explanation.ifPresent(exp -> explanations.put(result.getBookId(), exp));

            } catch (Exception e) {
                log.debug("Skipping explanation for book {} due to error", result.getBookId());
            }
        }

        log.info("Generated {} explanations for {} recommendations",
            explanations.size(), results.size());

        return explanations;
    }

    /**
     * AI 강화 기능 활성화 여부
     */
    public boolean isEnabled() {
        return enabled;
    }
}
