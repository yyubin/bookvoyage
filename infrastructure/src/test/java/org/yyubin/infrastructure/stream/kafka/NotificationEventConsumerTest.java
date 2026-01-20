package org.yyubin.infrastructure.stream.kafka;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.notification.NotificationMessages;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.user.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventConsumer 테스트")
class NotificationEventConsumerTest {

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private NotificationEventUseCase notificationEventUseCase;

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Test
    @DisplayName("REACTION_UPSERTED 이벤트는 좋아요 알림을 생성한다")
    void consume_Reaction_CreatesNotification() {
        // Given
        Review review = mock(Review.class);
        when(review.getUserId()).thenReturn(new UserId(2L));
        when(loadReviewPort.loadById(10L)).thenReturn(review);

        EventPayload payload = new EventPayload(
                null,
                "REACTION_UPSERTED",
                1L,
                null,
                null,
                Map.of("reviewId", 10L),
                null,
                null,
                1
        );

        // When
        consumer.consume(payload);

        // Then
        ArgumentCaptor<NotificationEventPayload> captor = ArgumentCaptor.forClass(NotificationEventPayload.class);
        verify(notificationEventUseCase).handle(captor.capture());
        NotificationEventPayload eventPayload = captor.getValue();
        assertThat(eventPayload.recipientId()).isEqualTo(2L);
        assertThat(eventPayload.type()).isEqualTo(NotificationType.LIKE_ON_REVIEW);
        assertThat(eventPayload.actorId()).isEqualTo(1L);
        assertThat(eventPayload.contentId()).isEqualTo(10L);
        assertThat(eventPayload.message()).isEqualTo(NotificationMessages.LIKE_ON_REVIEW);
    }

    @Test
    @DisplayName("자기 자신에게 반응한 경우 알림을 생성하지 않는다")
    void consume_Reaction_SelfLike_Skips() {
        // Given
        Review review = mock(Review.class);
        when(review.getUserId()).thenReturn(new UserId(1L));
        when(loadReviewPort.loadById(10L)).thenReturn(review);

        EventPayload payload = new EventPayload(
                null,
                "REACTION_UPSERTED",
                1L,
                null,
                null,
                Map.of("reviewId", 10L),
                null,
                null,
                1
        );

        // When
        consumer.consume(payload);

        // Then
        verify(notificationEventUseCase, never()).handle(any());
    }

    @Test
    @DisplayName("MENTION 이벤트는 멘션 알림을 생성한다")
    void consume_Mention_CreatesNotification() {
        // Given
        EventPayload payload = new EventPayload(
                null,
                "MENTION",
                1L,
                null,
                null,
                Map.of("mentionedUserId", 2L, "reviewId", 10L),
                null,
                null,
                1
        );

        // When
        consumer.consume(payload);

        // Then
        ArgumentCaptor<NotificationEventPayload> captor = ArgumentCaptor.forClass(NotificationEventPayload.class);
        verify(notificationEventUseCase).handle(captor.capture());
        NotificationEventPayload eventPayload = captor.getValue();
        assertThat(eventPayload.recipientId()).isEqualTo(2L);
        assertThat(eventPayload.type()).isEqualTo(NotificationType.MENTION);
        assertThat(eventPayload.actorId()).isEqualTo(1L);
        assertThat(eventPayload.contentId()).isEqualTo(10L);
        assertThat(eventPayload.message()).isEqualTo(NotificationMessages.MENTION);
    }

    @Test
    @DisplayName("USER_FOLLOWED 이벤트는 팔로우 알림을 생성한다")
    void consume_Follow_CreatesNotification() {
        // Given
        EventPayload payload = new EventPayload(
                null,
                "USER_FOLLOWED",
                1L,
                null,
                null,
                Map.of("followeeId", 2L),
                null,
                null,
                1
        );

        // When
        consumer.consume(payload);

        // Then
        ArgumentCaptor<NotificationEventPayload> captor = ArgumentCaptor.forClass(NotificationEventPayload.class);
        verify(notificationEventUseCase).handle(captor.capture());
        NotificationEventPayload eventPayload = captor.getValue();
        assertThat(eventPayload.recipientId()).isEqualTo(2L);
        assertThat(eventPayload.type()).isEqualTo(NotificationType.FOLLOWEE_NEW_REVIEW);
        assertThat(eventPayload.actorId()).isEqualTo(1L);
        assertThat(eventPayload.contentId()).isNull();
        assertThat(eventPayload.message()).isEqualTo(NotificationMessages.FOLLOWEE_NEW_REVIEW);
    }
}
