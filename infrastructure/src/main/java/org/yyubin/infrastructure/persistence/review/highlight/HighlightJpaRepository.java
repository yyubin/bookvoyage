package org.yyubin.infrastructure.persistence.review.highlight;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HighlightJpaRepository extends JpaRepository<HighlightEntity, Long> {

    Optional<HighlightEntity> findByNormalizedValue(String normalizedValue);

    List<HighlightEntity> findByIdIn(List<Long> ids);
}
