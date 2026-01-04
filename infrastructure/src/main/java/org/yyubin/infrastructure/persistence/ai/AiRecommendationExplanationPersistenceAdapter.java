package org.yyubin.infrastructure.persistence.ai;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.AiRecommendationExplanationPort;
import org.yyubin.domain.ai.AiRecommendationExplanationRecord;

@Component
@RequiredArgsConstructor
public class AiRecommendationExplanationPersistenceAdapter implements AiRecommendationExplanationPort {

    private final AiRecommendationExplanationJpaRepository explanationRepository;

    @Override
    public AiRecommendationExplanationRecord save(AiRecommendationExplanationRecord record) {
        AiRecommendationExplanationEntity saved =
            explanationRepository.save(AiRecommendationExplanationEntity.fromDomain(record));
        return saved.toDomain();
    }

    @Override
    public Optional<AiRecommendationExplanationRecord> findByUserIdAndBookId(Long userId, Long bookId) {
        return explanationRepository.findByUserIdAndBookId(userId, bookId)
            .map(AiRecommendationExplanationEntity::toDomain);
    }
}
