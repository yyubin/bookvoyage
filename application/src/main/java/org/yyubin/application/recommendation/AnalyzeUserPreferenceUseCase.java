package org.yyubin.application.recommendation;

import org.yyubin.domain.recommendation.UserAnalysis;

/**
 * 사용자 독서 취향 분석 Use Case
 * 사용자의 최근 활동을 기반으로 독서 성향을 분석하고 맞춤 도서를 추천
 */
public interface AnalyzeUserPreferenceUseCase {

    /**
     * 사용자 독서 취향 분석 실행
     *
     * @param userId 분석 대상 사용자 ID
     * @return 사용자 분석 결과 (성향, 키워드, 추천 도서 포함)
     */
    UserAnalysis execute(Long userId);
}
