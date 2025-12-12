package org.yyubin.recommendation.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

/**
 * 인기도 기반 스코어러
 * - 전체 사용자들의 행동 패턴 반영
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopularityScorer implements Scorer {

    private final BookDocumentRepository bookDocumentRepository;

    @Override
    public double score(Long userId, RecommendationCandidate candidate) {
        // 인기도 기반 후보는 initialScore를 그대로 사용
        if (candidate.getSource() == RecommendationCandidate.CandidateSource.POPULARITY) {
            return candidate.getInitialScore();
        }

        // TODO: Elasticsearch에서 bookId로 조회하여 실시간 인기도 계산
        // 현재는 간단히 처리
        return 0.5;
    }

    @Override
    public String getName() {
        return "PopularityScorer";
    }

    @Override
    public double getDefaultWeight() {
        return 0.1; // 인기도 점수 가중치 10%
    }
}
