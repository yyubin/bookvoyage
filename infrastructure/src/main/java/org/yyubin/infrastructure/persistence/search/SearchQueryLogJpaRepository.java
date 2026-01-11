package org.yyubin.infrastructure.persistence.search;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryLogJpaRepository extends JpaRepository<SearchQueryLogEntity, Long> {
    List<SearchQueryLogEntity> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
        Long userId,
        LocalDateTime createdAt,
        Pageable pageable
    );
}
