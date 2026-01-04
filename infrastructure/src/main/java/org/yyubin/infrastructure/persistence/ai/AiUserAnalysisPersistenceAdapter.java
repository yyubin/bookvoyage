package org.yyubin.infrastructure.persistence.ai;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.AiUserAnalysisPort;
import org.yyubin.domain.ai.AiUserAnalysisRecord;
import org.yyubin.domain.ai.AiUserAnalysisRecommendation;

@Component
@RequiredArgsConstructor
public class AiUserAnalysisPersistenceAdapter implements AiUserAnalysisPort {

    private final AiUserAnalysisJpaRepository analysisRepository;
    private final AiUserAnalysisRecommendationJpaRepository recommendationRepository;

    @Override
    public AiUserAnalysisRecord save(AiUserAnalysisRecord record) {
        AiUserAnalysisEntity saved = analysisRepository.save(AiUserAnalysisEntity.fromDomain(record));

        List<AiUserAnalysisRecommendation> recommendations =
            record.recommendations() != null ? record.recommendations() : Collections.emptyList();

        if (!recommendations.isEmpty()) {
            List<AiUserAnalysisRecommendationEntity> entities = recommendations.stream()
                .map(rec -> AiUserAnalysisRecommendationEntity.fromDomain(
                    AiUserAnalysisRecommendation.of(
                        rec.id(),
                        saved.getId(),
                        rec.bookId(),
                        rec.bookTitle(),
                        rec.author(),
                        rec.reason(),
                        rec.rank()
                    )
                ))
                .toList();
            recommendationRepository.saveAll(entities);
        }

        List<AiUserAnalysisRecommendation> savedRecs =
            recommendationRepository.findByAnalysisIdOrderByRankAsc(saved.getId()).stream()
                .map(AiUserAnalysisRecommendationEntity::toDomain)
                .collect(Collectors.toList());

        return saved.toDomain(savedRecs);
    }

    @Override
    public Optional<AiUserAnalysisRecord> findLatestByUserId(Long userId) {
        return analysisRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
            .map(entity -> entity.toDomain(
                recommendationRepository.findByAnalysisIdOrderByRankAsc(entity.getId()).stream()
                    .map(AiUserAnalysisRecommendationEntity::toDomain)
                    .collect(Collectors.toList())
            ));
    }

    @Override
    public Optional<AiUserAnalysisRecord> findById(Long id) {
        return analysisRepository.findById(id)
            .map(entity -> entity.toDomain(
                recommendationRepository.findByAnalysisIdOrderByRankAsc(entity.getId()).stream()
                    .map(AiUserAnalysisRecommendationEntity::toDomain)
                    .collect(Collectors.toList())
            ));
    }
}
