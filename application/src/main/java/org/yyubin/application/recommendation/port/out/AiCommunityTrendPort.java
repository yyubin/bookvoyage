package org.yyubin.application.recommendation.port.out;

import java.time.LocalDateTime;
import java.util.Optional;
import org.yyubin.domain.ai.AiCommunityTrendRecord;

public interface AiCommunityTrendPort {

    AiCommunityTrendRecord save(AiCommunityTrendRecord record);

    Optional<AiCommunityTrendRecord> findLatest();

    Optional<AiCommunityTrendRecord> findLatestByWindow(LocalDateTime windowStart, LocalDateTime windowEnd);
}
