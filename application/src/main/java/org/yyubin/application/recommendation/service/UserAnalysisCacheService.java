package org.yyubin.application.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.application.recommendation.port.out.AiUserAnalysisPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.ai.AiUserAnalysisRecord;
import org.yyubin.domain.recommendation.UserAnalysis;
import org.yyubin.domain.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 분석 캐시 관리 서비스
 * 시맨틱 캐시 및 데이터베이스를 통한 분석 결과 조회 및 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalysisCacheService {

    private final SemanticCachePort cachePort;
    private final AiUserAnalysisPort analysisPort;
    private final UserAnalysisLLMService llmService;
    private final ObjectMapper objectMapper;

    private static final String CACHE_CATEGORY = "user_analysis";

    /**
     * 사용자별 날짜 기반 캐시 키 생성
     *
     * @param user 사용자 정보
     * @return 캐시 키 문자열
     */
    public String buildCacheKey(User user) {
        return String.format("user_analysis_%s_%s", user.id().value(), LocalDate.now());
    }

    /**
     * 캐시에서 분석 결과 조회
     *
     * @param cacheKey 캐시 키
     * @param userId 사용자 ID
     * @return 캐시된 분석 결과 (있는 경우)
     */
    public Optional<UserAnalysis> getFromCache(String cacheKey, Long userId) {
        return cachePort.get(cacheKey, CACHE_CATEGORY)
            .map(response -> llmService.parseAnalysisFromJson(userId, response));
    }

    /**
     * 데이터베이스에서 오늘 날짜의 분석 결과 조회
     *
     * @param userId 사용자 ID
     * @return 오늘 날짜의 분석 결과 (있는 경우)
     */
    public Optional<UserAnalysis> getFromDatabase(Long userId) {
        Optional<AiUserAnalysisRecord> latest = analysisPort.findLatestByUserId(userId);
        if (latest.isPresent()
            && latest.get().status() == AiResultStatus.SUCCESS
            && isSameDay(latest.get().generatedAt(), LocalDate.now())) {
            return Optional.of(toUserAnalysis(latest.get()));
        }
        return Optional.empty();
    }

    /**
     * 분석 결과를 캐시에 저장
     *
     * @param cacheKey 캐시 키
     * @param analysis 분석 결과
     */
    public void saveToCache(String cacheKey, UserAnalysis analysis) {
        String cachePayload = serializeAnalysis(analysis);
        if (!cachePayload.isBlank()) {
            cachePort.put(cacheKey, cachePayload, CACHE_CATEGORY);
        }
    }

    /**
     * UserAnalysis를 JSON 문자열로 직렬화
     *
     * @param analysis 분석 결과
     * @return JSON 문자열
     */
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

    /**
     * 두 날짜가 같은 날인지 비교
     *
     * @param left LocalDateTime
     * @param right LocalDate
     * @return 같은 날이면 true
     */
    private boolean isSameDay(LocalDateTime left, LocalDate right) {
        if (left == null || right == null) {
            return false;
        }
        return left.toLocalDate().isEqual(right);
    }

    /**
     * AiUserAnalysisRecord를 UserAnalysis로 변환
     *
     * @param record DB 레코드
     * @return 도메인 모델
     */
    private UserAnalysis toUserAnalysis(AiUserAnalysisRecord record) {
        List<UserAnalysis.BookRecommendation> recommendations = new java.util.ArrayList<>();
        if (record.recommendations() != null) {
            for (var rec : record.recommendations()) {
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
}
