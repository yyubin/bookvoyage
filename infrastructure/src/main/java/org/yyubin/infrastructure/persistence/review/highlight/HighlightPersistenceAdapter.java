package org.yyubin.infrastructure.persistence.review.highlight;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.HighlightRepository;
import org.yyubin.domain.review.Highlight;
import org.yyubin.domain.review.HighlightId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighlightPersistenceAdapter implements HighlightRepository {

    private final HighlightJpaRepository highlightJpaRepository;

    @Override
    public Optional<Highlight> findByNormalizedValue(String normalizedValue) {
        return highlightJpaRepository.findByNormalizedValue(normalizedValue)
                .map(HighlightEntity::toDomain);
    }

    @Override
    @Transactional
    public Highlight save(Highlight highlight) {
        HighlightEntity saved = highlightJpaRepository.save(HighlightEntity.fromDomain(highlight));
        return saved.toDomain();
    }

    @Override
    public List<Highlight> findAllByIds(List<HighlightId> ids) {
        List<Long> rawIds = ids.stream().map(HighlightId::value).toList();
        return highlightJpaRepository.findByIdIn(rawIds).stream()
                .map(HighlightEntity::toDomain)
                .toList();
    }
}
