package org.yyubin.infrastructure.persistence.review;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.KeywordRepository;
import org.yyubin.domain.review.Keyword;
import org.yyubin.domain.review.KeywordId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordPersistenceAdapter implements KeywordRepository {

    private final KeywordJpaRepository keywordJpaRepository;

    @Override
    public Optional<Keyword> findByNormalizedValue(String normalizedValue) {
        return keywordJpaRepository.findByNormalizedValue(normalizedValue)
                .map(KeywordEntity::toDomain);
    }

    @Override
    @Transactional
    public Keyword save(Keyword keyword) {
        KeywordEntity saved = keywordJpaRepository.save(KeywordEntity.fromDomain(keyword));
        return saved.toDomain();
    }

    @Override
    public List<Keyword> findAllByIds(List<KeywordId> ids) {
        List<Long> rawIds = ids.stream().map(KeywordId::value).toList();
        return keywordJpaRepository.findByIdIn(rawIds).stream()
                .map(KeywordEntity::toDomain)
                .toList();
    }
}
