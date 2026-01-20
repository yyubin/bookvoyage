package org.yyubin.infrastructure.feed;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.feed.FeedItemPersistenceAdapter;
import org.yyubin.infrastructure.stream.feed.RedisFeedItemAdapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DualFeedItemWriter 테스트")
class DualFeedItemWriterTest {

    @Mock
    private RedisFeedItemAdapter redisWriter;

    @Mock
    private FeedItemPersistenceAdapter dbWriter;

    @InjectMocks
    private DualFeedItemWriter dualFeedItemWriter;

    @Test
    @DisplayName("DB와 Redis에 모두 저장한다")
    void save_WritesToBothDbAndRedis() {
        // Given
        FeedItem feedItem = createFeedItem(null);
        FeedItem savedItem = createFeedItem(1L);
        when(dbWriter.save(feedItem)).thenReturn(savedItem);

        // When
        FeedItem result = dualFeedItemWriter.save(feedItem);

        // Then
        assertThat(result).isEqualTo(savedItem);
        verify(dbWriter).save(feedItem);
        verify(redisWriter).save(savedItem);
    }

    @Test
    @DisplayName("Redis 저장 실패해도 DB 저장 결과를 반환한다")
    void save_RedisFailure_StillReturnsDbResult() {
        // Given
        FeedItem feedItem = createFeedItem(null);
        FeedItem savedItem = createFeedItem(1L);
        when(dbWriter.save(feedItem)).thenReturn(savedItem);
        doThrow(new RuntimeException("Redis connection failed")).when(redisWriter).save(any(FeedItem.class));

        // When
        FeedItem result = dualFeedItemWriter.save(feedItem);

        // Then
        assertThat(result).isEqualTo(savedItem);
        verify(dbWriter).save(feedItem);
        verify(redisWriter).save(savedItem);
    }

    @Test
    @DisplayName("저장된 아이템이 Redis에 전달된다")
    void save_PassesSavedItemToRedis() {
        // Given
        FeedItem feedItem = createFeedItem(null);
        FeedItem savedItem = createFeedItem(100L);
        when(dbWriter.save(feedItem)).thenReturn(savedItem);

        // When
        dualFeedItemWriter.save(feedItem);

        // Then
        verify(redisWriter).save(savedItem);
    }

    private FeedItem createFeedItem(Long id) {
        return FeedItem.of(
            id,
            new UserId(1L),
            ReviewId.of(10L),
            LocalDateTime.of(2024, 1, 1, 10, 0)
        );
    }
}
