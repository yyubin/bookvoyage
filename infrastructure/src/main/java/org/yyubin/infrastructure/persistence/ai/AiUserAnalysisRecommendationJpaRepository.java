package org.yyubin.infrastructure.persistence.ai;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUserAnalysisRecommendationJpaRepository
    extends JpaRepository<AiUserAnalysisRecommendationEntity, Long> {

    List<AiUserAnalysisRecommendationEntity> findByAnalysisIdOrderByRankAsc(Long analysisId);
}
