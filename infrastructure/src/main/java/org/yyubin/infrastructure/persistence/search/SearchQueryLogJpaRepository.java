package org.yyubin.infrastructure.persistence.search;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryLogJpaRepository extends JpaRepository<SearchQueryLogEntity, Long> {
}
