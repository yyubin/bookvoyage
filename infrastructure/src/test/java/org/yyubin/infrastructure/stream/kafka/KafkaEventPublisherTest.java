package org.yyubin.infrastructure.stream.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.yyubin.application.event.EventPayload;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisher 테스트")
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, EventPayload> kafkaTemplate;

    @InjectMocks
    private KafkaEventPublisher publisher;

    @Test
    @DisplayName("토픽, 키, 페이로드를 Kafka로 전송한다")
    void publish_SendsToKafka() {
        // Given
        EventPayload payload = new EventPayload(null, "EVENT", 1L, null, null, null, null, null, 1);

        // When
        publisher.publish("topic", "key", payload);

        // Then
        verify(kafkaTemplate).send("topic", "key", payload);
    }
}
