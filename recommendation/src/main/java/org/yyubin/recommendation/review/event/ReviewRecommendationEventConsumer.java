package org.yyubin.recommendation.review.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewRecommendationEventConsumer {

    private final ReviewRecommendationEventHandler handler;

    @KafkaListener(
            topics = EventTopics.REVIEW_SEARCH_INDEX,
            groupId = "${spring.kafka.consumer.group-id:cg-review-recommendation}",
            containerFactory = "reviewSearchIndexKafkaListenerContainerFactory"
    )
    public void consume(ReviewSearchIndexEvent event) {
        if (event == null) {
            return;
        }
        handler.handle(event);
    }
}
