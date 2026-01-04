package org.yyubin.infrastructure.persistence.ai;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiPromptJpaRepository extends JpaRepository<AiPromptEntity, Long> {

    Optional<AiPromptEntity> findByPromptKey(String promptKey);
}
