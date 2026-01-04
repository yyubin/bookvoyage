package org.yyubin.application.recommendation.port.out;

import java.util.Optional;
import org.yyubin.domain.ai.AiPrompt;
import org.yyubin.domain.ai.AiPromptVersion;

public interface AiPromptPort {

    AiPrompt save(AiPrompt prompt);

    Optional<AiPrompt> findByPromptKey(String promptKey);

    AiPromptVersion saveVersion(AiPromptVersion version);

    Optional<AiPromptVersion> findActiveVersionByPromptKey(String promptKey);

    Optional<AiPromptVersion> findVersionById(Long versionId);
}
