package org.yyubin.recommendation.review.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.recommendation.review.RecommendationIngestCommand;
import org.yyubin.recommendation.review.HighlightReviewRecommendationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewRecommendationEventHandler {

    private final HighlightReviewRecommendationService reviewRecommendationService;
    private final HighlightNormalizer highlightNormalizer;

    public void handle(ReviewSearchIndexEvent event) {
        if (event == null || event.type() == null) {
            return;
        }
        if (event.type() == ReviewSearchIndexEventType.DELETE) {
            if (event.reviewId() != null) {
                reviewRecommendationService.deleteReview(event.reviewId());
            }
            return;
        }

        if (event.reviewId() == null || event.userId() == null || event.bookId() == null) {
            log.debug("Skip ingest due to missing ids reviewId={} userId={} bookId={}",
                    event.reviewId(), event.userId(), event.bookId());
            return;
        }

        List<String> highlights = event.highlights() != null ? event.highlights() : List.of();
        List<String> highlightsNorm = event.highlightsNorm() != null ? event.highlightsNorm() : List.of();
        if (highlightsNorm.isEmpty() && !highlights.isEmpty()) {
            highlightsNorm = highlights.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(highlightNormalizer::normalize)
                    .toList();
        }

        RecommendationIngestCommand command = new RecommendationIngestCommand(
                event.reviewId(),
                event.userId(),
                event.bookId(),
                event.bookTitle(),
                event.summary(),
                event.content(),
                highlights,
                highlightsNorm,
                event.keywords() != null ? event.keywords() : List.of(),
                event.genre(),
                event.createdAt(),
                event.rating()
        );
        reviewRecommendationService.ingest(command);
    }
}
