package org.yyubin.infrastructure.persistence.ai;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.AiPromptPort;
import org.yyubin.domain.ai.AiPrompt;
import org.yyubin.domain.ai.AiPromptVersion;

@Component
@RequiredArgsConstructor
public class AiPromptPersistenceAdapter implements AiPromptPort {

    private final AiPromptJpaRepository promptRepository;
    private final AiPromptVersionJpaRepository versionRepository;

    @Override
    public AiPrompt save(AiPrompt prompt) {
        AiPromptEntity saved = promptRepository.save(AiPromptEntity.fromDomain(prompt));
        return saved.toDomain();
    }

    @Override
    public Optional<AiPrompt> findByPromptKey(String promptKey) {
        return promptRepository.findByPromptKey(promptKey).map(AiPromptEntity::toDomain);
    }

    @Override
    public AiPromptVersion saveVersion(AiPromptVersion version) {
        AiPromptVersionEntity saved = versionRepository.save(AiPromptVersionEntity.fromDomain(version));
        return saved.toDomain();
    }

    @Override
    public Optional<AiPromptVersion> findActiveVersionByPromptKey(String promptKey) {
        Optional<AiPromptEntity> prompt = promptRepository.findByPromptKey(promptKey);
        if (prompt.isEmpty()) {
            return Optional.empty();
        }
        return versionRepository
            .findFirstByPromptIdAndActiveTrueOrderByVersionDesc(prompt.get().getId())
            .map(AiPromptVersionEntity::toDomain);
    }

    @Override
    public Optional<AiPromptVersion> findVersionById(Long versionId) {
        return versionRepository.findById(versionId).map(AiPromptVersionEntity::toDomain);
    }
}
