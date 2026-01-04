package org.yyubin.application.recommendation.port.out;

import java.util.Optional;
import org.yyubin.domain.ai.AiUserAnalysisRecord;

public interface AiUserAnalysisPort {

    AiUserAnalysisRecord save(AiUserAnalysisRecord record);

    Optional<AiUserAnalysisRecord> findLatestByUserId(Long userId);

    Optional<AiUserAnalysisRecord> findById(Long id);
}
