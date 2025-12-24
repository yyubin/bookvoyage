package org.yyubin.recommendation.review.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewRecommendationEventConsumer {

    private final ReviewRecommendationEventHandler handler;

    @KafkaListener(
            topics = "events.review",
            groupId = "${spring.kafka.consumer.group-id:cg-review-recommendation}",
            containerFactory = "recommendationKafkaListenerContainerFactory"
    )
    public void consume(EventPayload payload) {
        if (payload == null) {
            return;
        }
        handler.handle(payload);
    }
}
