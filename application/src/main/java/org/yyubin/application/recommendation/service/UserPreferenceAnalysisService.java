package org.yyubin.application.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.AnalyzeUserPreferenceUseCase;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.recommendation.UserAnalysis;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 사용자 독서 취향 분석 오케스트레이터 서비스
 * 캐시, 데이터베이스, LLM을 조율하여 사용자 분석 수행
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
public class UserPreferenceAnalysisService implements AnalyzeUserPreferenceUseCase {

    private final LoadUserPort loadUserPort;
    private final UserAnalysisContextPort userAnalysisContextPort;
    private final UserAnalysisLLMService llmService;
    private final UserAnalysisValidationService validationService;
    private final UserAnalysisCacheService cacheService;
    private final UserAnalysisPersistenceService persistenceService;

    private static final int RECENT_REVIEW_LIMIT = 8;
    private static final int RECENT_LIBRARY_LIMIT = 8;
    private static final int RECENT_SEARCH_LIMIT = 10;
    private static final int RECENT_SEARCH_DAYS = 30;

    @Override
    public UserAnalysis execute(Long userId) {
        log.info("Analyzing user preference for userId={}", userId);

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
        String cacheKey = cacheService.buildCacheKey(user);

        // 4. SemanticCache 확인
        Optional<UserAnalysis> cached = cacheService.getFromCache(cacheKey, userId);
        if (cached.isPresent()) {
            log.debug("Cache HIT for key={}", cacheKey);
            return cached.get();
        }

        // 5. 캐시 미스 - 데이터베이스 또는 LLM 분석
        return loadFromDatabaseOrAnalyze(user, context, cacheKey);
    }

    /**
     * 데이터베이스에서 오늘 날짜 분석 조회 또는 LLM 분석 수행
     *
     * @param user 사용자 정보
     * @param context 사용자 활동 컨텍스트
     * @param cacheKey 캐시 키
     * @return 사용자 분석 결과
     */
    private UserAnalysis loadFromDatabaseOrAnalyze(
        User user,
        UserAnalysisContextPort.UserAnalysisContext context,
        String cacheKey
    ) {
        // 데이터베이스에서 오늘 날짜의 분석 조회
        Optional<UserAnalysis> fromDb = cacheService.getFromDatabase(user.id().value());
        if (fromDb.isPresent()) {
            log.debug("Database HIT for userId={}", user.id());
            UserAnalysis analysis = fromDb.get();
            // 캐시에 저장
            cacheService.saveToCache(cacheKey, analysis);
            return analysis;
        }

        // 데이터베이스에도 없음 - LLM 분석 수행
        return analyzeWithLLM(user, context, cacheKey);
    }

    /**
     * LLM을 사용하여 사용자 분석 수행
     *
     * @param user 사용자 정보
     * @param context 사용자 활동 컨텍스트
     * @param cacheKey 캐시 키
     * @return 사용자 분석 결과
     */
    private UserAnalysis analyzeWithLLM(
        User user,
        UserAnalysisContextPort.UserAnalysisContext context,
        String cacheKey
    ) {
        log.info("Cache MISS - Analyzing user {} with LLM", user.id());

        // LLM 호출
        UserAnalysisLLMService.LLMAnalysisResult llmResult = llmService.analyzeWithLLM(user, context);

        // 추천 도서 검증
        UserAnalysis analysis = validationService.validateRecommendations(llmResult.analysis());

        // 캐시 저장
        cacheService.saveToCache(cacheKey, analysis);

        // 데이터베이스 영속화
        persistenceService.persistAnalysis(
            user.id().value(),
            llmResult.rawResponse(),
            analysis,
            cacheKey
        );

        return analysis;
    }
}
