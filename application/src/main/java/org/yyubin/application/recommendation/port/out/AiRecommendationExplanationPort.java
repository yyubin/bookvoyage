package org.yyubin.application.recommendation.port.out;

import java.util.Optional;
import org.yyubin.domain.ai.AiRecommendationExplanationRecord;

public interface AiRecommendationExplanationPort {

    AiRecommendationExplanationRecord save(AiRecommendationExplanationRecord record);

    Optional<AiRecommendationExplanationRecord> findByUserIdAndBookId(Long userId, Long bookId);
}
