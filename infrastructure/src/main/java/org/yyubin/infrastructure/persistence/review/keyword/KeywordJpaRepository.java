package org.yyubin.infrastructure.persistence.review.keyword;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordJpaRepository extends JpaRepository<KeywordEntity, Long> {

    Optional<KeywordEntity> findByNormalizedValue(String normalizedValue);

    List<KeywordEntity> findByIdIn(List<Long> ids);
}
