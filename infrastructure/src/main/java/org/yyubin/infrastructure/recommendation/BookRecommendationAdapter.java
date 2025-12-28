package org.yyubin.infrastructure.recommendation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.BookRecommendationPort;
import org.yyubin.recommendation.service.RecommendationResult;
import org.yyubin.recommendation.service.RecommendationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookRecommendationAdapter implements BookRecommendationPort {

    private final RecommendationService recommendationService;

    @Override
    public List<RecommendationItem> getRecommendations(Long userId, int limit, boolean forceRefresh) {
        List<RecommendationResult> results = recommendationService.generateRecommendations(
                userId,
                limit,
                forceRefresh
        );

        return results.stream()
                .map(r -> new RecommendationItem(
                        r.getBookId(),
                        r.getScore(),
                        r.getRank(),
                        r.getSource(),
                        r.getReason()
                ))
                .toList();
    }
}
