package org.yyubin.infrastructure.stream.outbox;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.OutboxPort;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventPublisher 테스트")
class OutboxEventPublisherTest {

    @Mock
    private OutboxPort outboxPort;

    @InjectMocks
    private OutboxEventPublisher publisher;

    @Test
    @DisplayName("이벤트를 Outbox로 저장한다")
    void publish_SavesToOutbox() {
        // Given
        EventPayload payload = new EventPayload(null, "EVENT", 1L, null, null, null, null, null, 1);

        // When
        publisher.publish("topic", "key", payload);

        // Then
        verify(outboxPort).save("topic", "key", payload);
    }
}
