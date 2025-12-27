package org.yyubin.infrastructure.stream.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventPublisher;

@Component
@RequiredArgsConstructor
public class ReviewSearchIndexKafkaPublisher implements ReviewSearchIndexEventPublisher {

    private final KafkaTemplate<String, ReviewSearchIndexEvent> reviewSearchIndexKafkaTemplate;

    @Override
    public void publish(ReviewSearchIndexEvent event) {
        if (event == null || event.reviewId() == null) {
            return;
        }
        reviewSearchIndexKafkaTemplate.send(
                EventTopics.REVIEW_SEARCH_INDEX,
                event.reviewId().toString(),
                event
        );
    }
}
