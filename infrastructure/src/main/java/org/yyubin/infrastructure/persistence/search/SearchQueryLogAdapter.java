package org.yyubin.infrastructure.persistence.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.search.port.SearchQueryLogPort;
import org.yyubin.domain.search.SearchQuery;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchQueryLogAdapter implements SearchQueryLogPort {

    private final SearchQueryLogJpaRepository repository;

    @Override
    @Transactional
    public void save(SearchQuery searchQuery) {
        SearchQueryLogEntity entity = SearchQueryLogEntity.fromDomain(searchQuery);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void saveBatch(List<SearchQuery> searchQueries) {
        if (searchQueries.isEmpty()) {
            return;
        }

        List<SearchQueryLogEntity> entities = searchQueries.stream()
            .map(SearchQueryLogEntity::fromDomain)
            .toList();

        repository.saveAll(entities);
        log.info("Saved {} search query logs to database", entities.size());
    }
}
