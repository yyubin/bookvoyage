package org.yyubin.application.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.application.recommendation.port.out.AiUserAnalysisPort;
import org.yyubin.domain.ai.AiPromptVersion;
import org.yyubin.domain.ai.AiResultStatus;
import org.yyubin.domain.ai.AiUserAnalysisRecord;
import org.yyubin.domain.ai.AiUserAnalysisRecommendation;
import org.yyubin.domain.recommendation.UserAnalysis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 분석 영속화 서비스
 * 분석 결과를 데이터베이스에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAnalysisPersistenceService {

    private final AiUserAnalysisPort analysisPort;
    private final AiPromptPort promptPort;

    private static final String PROMPT_KEY = "user_analysis";
    private static final int CACHE_TTL_HOURS = 24;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AiUserAnalysisRecord record) {
        analysisPort.save(record);
    }

    /**
     * UserAnalysis를 데이터베이스에 저장
     * 프롬프트 버전과 함께 레코드를 생성하여 저장
     *
     * @param userId 사용자 ID
     * @param rawResponse LLM 원본 응답
     * @param analysis 파싱된 분석 결과
     * @param cacheKey 캐시 키
     */
    public void persistAnalysis(
        Long userId,
        String rawResponse,
        UserAnalysis analysis,
        String cacheKey
    ) {
        AiPromptVersion promptVersion = promptPort
            .findActiveVersionByPromptKey(PROMPT_KEY)
            .orElse(null);

        if (promptVersion == null) {
            log.warn("No active prompt version for {} - skipping persistence", PROMPT_KEY);
            return;
        }

        List<AiUserAnalysisRecommendation> recommendations = buildRecommendationRecords(analysis.recommendations());

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
            now.plusHours(CACHE_TTL_HOURS),
            AiResultStatus.SUCCESS,
            null,
            recommendations
        );

        save(record);
    }

    /**
     * BookRecommendation 리스트를 AiUserAnalysisRecommendation 리스트로 변환
     *
     * @param recommendations 추천 도서 리스트
     * @return DB용 추천 레코드 리스트
     */
    private List<AiUserAnalysisRecommendation> buildRecommendationRecords(
        List<UserAnalysis.BookRecommendation> recommendations
    ) {
        if (recommendations == null) {
            return new ArrayList<>();
        }

        List<AiUserAnalysisRecommendation> records = new ArrayList<>();
        int rank = 1;
        for (UserAnalysis.BookRecommendation rec : recommendations) {
            records.add(AiUserAnalysisRecommendation.of(
                null,
                null,
                null,
                rec.bookTitle(),
                rec.author(),
                rec.reason(),
                rank++
            ));
        }
        return records;
    }
}
