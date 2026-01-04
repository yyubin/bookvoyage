package org.yyubin.infrastructure.persistence.ai;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiCommunityTrendJpaRepository extends JpaRepository<AiCommunityTrendEntity, Long> {

    Optional<AiCommunityTrendEntity> findFirstByOrderByGeneratedAtDesc();

    Optional<AiCommunityTrendEntity> findFirstByWindowStartAndWindowEndOrderByGeneratedAtDesc(
        LocalDateTime windowStart,
        LocalDateTime windowEnd
    );
}
