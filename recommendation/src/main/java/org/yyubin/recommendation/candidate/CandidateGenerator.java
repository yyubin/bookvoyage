package org.yyubin.recommendation.candidate;

import java.util.List;

/**
 * 후보 생성기 인터페이스
 */
public interface CandidateGenerator {

    /**
     * 특정 사용자에 대한 추천 후보 생성
     *
     * @param userId 사용자 ID
     * @param limit 생성할 후보 수
     * @return 추천 후보 리스트
     */
    List<RecommendationCandidate> generateCandidates(Long userId, int limit);

    /**
     * 이 생성기가 지원하는 소스 타입
     */
    RecommendationCandidate.CandidateSource getSourceType();
}
