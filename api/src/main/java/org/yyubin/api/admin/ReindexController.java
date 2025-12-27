package org.yyubin.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventPublisher;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ReindexController {

    private final LoadReviewPort loadReviewPort;
    private final LoadBookPort loadBookPort;
    private final LoadKeywordsUseCase loadKeywordsUseCase;
    private final LoadHighlightsUseCase loadHighlightsUseCase;
    private final EventPublisher eventPublisher;
    private final ReviewSearchIndexEventPublisher reviewSearchIndexEventPublisher;

    @PostMapping("/reindex-reviews")
    public ResponseEntity<Map<String, Object>> reindexAllReviews() {
        log.info("Starting review reindexing...");

        List<Review> reviews = loadReviewPort.findAll();
        int successCount = 0;
        int failCount = 0;

        for (Review review : reviews) {
            try {
                if (review.isDeleted()) {
                    continue;
                }

                Book book = loadBookPort.loadById(review.getBookId().getValue())
                        .orElse(null);

                if (book == null) {
                    log.warn("Book not found for review {}", review.getId().getValue());
                    failCount++;
                    continue;
                }

                List<String> keywords = loadKeywordsUseCase.loadKeywords(review.getId());
                List<String> highlights = loadHighlightsUseCase.loadHighlights(review.getId());

                publishReviewEvent(review, book, keywords, highlights);
                successCount++;

                log.info("Reindexed review {} - {}", review.getId().getValue(), book.getMetadata().getTitle());
            } catch (Exception e) {
                log.error("Failed to reindex review {}", review.getId().getValue(), e);
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", reviews.size());
        result.put("success", successCount);
        result.put("failed", failCount);

        log.info("Reindexing completed: {}", result);
        return ResponseEntity.ok(result);
    }

    private void publishReviewEvent(Review review, Book book, List<String> keywords, List<String> highlights) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("bookId", book.getId().getValue());
        metadata.put("bookTitle", book.getMetadata().getTitle());
        metadata.put("rating", review.getRating().getValue());
        metadata.put("visibility", review.getVisibility().name());
        metadata.put("reviewId", review.getId() != null ? review.getId().getValue() : null);
        metadata.put("summary", review.getSummary());
        metadata.put("content", review.getContent());
        metadata.put("genre", review.getGenre().name());
        metadata.put("createdAt", review.getCreatedAt().toString());
        metadata.put("keywords", keywords);
        metadata.put("highlights", highlights);
        metadata.put("highlightsNorm", normalizeHighlights(highlights));

        eventPublisher.publish(
                EventTopics.REVIEW,
                review.getUserId().value().toString(),
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        "REVIEW_UPDATED",
                        review.getUserId().value(),
                        "REVIEW",
                        review.getId() != null ? review.getId().getValue().toString() : null,
                        metadata,
                        java.time.Instant.now(),
                        "reindex-api",
                        1
                )
        );

        ReviewSearchIndexEvent indexEvent = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                review.getId() != null ? review.getId().getValue() : null,
                review.getUserId().value(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                review.getSummary(),
                review.getContent(),
                highlights,
                normalizeHighlights(highlights),
                keywords,
                review.getGenre().name(),
                review.getCreatedAt(),
                review.getRating().getValue()
        );
        reviewSearchIndexEventPublisher.publish(indexEvent);
    }

    private List<String> normalizeHighlights(List<String> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return highlights.stream()
                .filter(h -> h != null && !h.isBlank())
                .map(String::trim)
                .toList();
    }
}
