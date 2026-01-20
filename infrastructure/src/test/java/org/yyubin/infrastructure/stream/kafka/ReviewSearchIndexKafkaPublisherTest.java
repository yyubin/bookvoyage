package org.yyubin.infrastructure.stream.kafka;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewSearchIndexKafkaPublisher 테스트")
class ReviewSearchIndexKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, ReviewSearchIndexEvent> kafkaTemplate;

    @InjectMocks
    private ReviewSearchIndexKafkaPublisher publisher;

    @Test
    @DisplayName("null 이벤트면 전송하지 않는다")
    void publish_NullEvent_NoSend() {
        // When
        publisher.publish(null);

        // Then
        verify(kafkaTemplate, never()).send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("reviewId가 없으면 전송하지 않는다")
    void publish_NullReviewId_NoSend() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                null,
                1L,
                2L,
                "title",
                "summary",
                "content",
                List.of(),
                List.of(),
                List.of(),
                "genre",
                LocalDateTime.now(),
                5
        );

        // When
        publisher.publish(event);

        // Then
        verify(kafkaTemplate, never()).send(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("정상 이벤트면 전송한다")
    void publish_ValidEvent_Sends() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                10L,
                1L,
                2L,
                "title",
                "summary",
                "content",
                List.of(),
                List.of(),
                List.of(),
                "genre",
                LocalDateTime.now(),
                5
        );

        // When
        publisher.publish(event);

        // Then
        verify(kafkaTemplate).send(EventTopics.REVIEW_SEARCH_INDEX, "10", event);
    }
}
