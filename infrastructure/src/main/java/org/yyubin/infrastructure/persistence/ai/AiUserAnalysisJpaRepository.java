package org.yyubin.infrastructure.persistence.ai;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiUserAnalysisJpaRepository extends JpaRepository<AiUserAnalysisEntity, Long> {

    Optional<AiUserAnalysisEntity> findFirstByUserIdOrderByGeneratedAtDesc(Long userId);
}
