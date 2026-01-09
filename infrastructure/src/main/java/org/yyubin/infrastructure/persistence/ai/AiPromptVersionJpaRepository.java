package org.yyubin.infrastructure.persistence.ai;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptVersionJpaRepository extends JpaRepository<AiPromptVersionEntity, Long> {

    Optional<AiPromptVersionEntity> findFirstByPromptIdAndActiveTrueOrderByVersionDesc(Long promptId);

    Optional<AiPromptVersionEntity> findFirstByPromptIdOrderByVersionDesc(Long promptId);

    Optional<AiPromptVersionEntity> findByPromptIdAndVersion(Long promptId, int version);
}
