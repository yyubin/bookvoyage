package org.yyubin.recommendation.tracking;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;

@Component
@RequiredArgsConstructor
public class RecommendationTrackingEventConsumer {

    private final RecommendationTrackingEventHandler handler;

    @KafkaListener(
            topics = "events.tracking",
            groupId = "${spring.kafka.consumer.group-id:cg-recommendation-tracking}",
            containerFactory = "recommendationKafkaListenerContainerFactory"
    )
    public void consume(EventPayload payload) {
        if (payload == null) {
            return;
        }
        handler.handle(payload);
    }
}
