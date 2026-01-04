package org.yyubin.infrastructure.persistence.ai;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiRecommendationExplanationJpaRepository
    extends JpaRepository<AiRecommendationExplanationEntity, Long> {

    Optional<AiRecommendationExplanationEntity> findByUserIdAndBookId(Long userId, Long bookId);
}
