package org.yyubin.infrastructure.persistence.ai;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.AiCommunityTrendPort;
import org.yyubin.domain.ai.AiCommunityTrendRecord;

@Component
@RequiredArgsConstructor
public class AiCommunityTrendPersistenceAdapter implements AiCommunityTrendPort {

    private final AiCommunityTrendJpaRepository trendRepository;

    @Override
    public AiCommunityTrendRecord save(AiCommunityTrendRecord record) {
        AiCommunityTrendEntity saved = trendRepository.save(AiCommunityTrendEntity.fromDomain(record));
        return saved.toDomain();
    }

    @Override
    public Optional<AiCommunityTrendRecord> findLatest() {
        return trendRepository.findFirstByOrderByGeneratedAtDesc()
            .map(AiCommunityTrendEntity::toDomain);
    }

    @Override
    public Optional<AiCommunityTrendRecord> findLatestByWindow(
        LocalDateTime windowStart,
        LocalDateTime windowEnd
    ) {
        return trendRepository
            .findFirstByWindowStartAndWindowEndOrderByGeneratedAtDesc(windowStart, windowEnd)
            .map(AiCommunityTrendEntity::toDomain);
    }
}
