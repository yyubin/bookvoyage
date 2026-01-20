package org.yyubin.infrastructure.stream.feed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisFeedItemAdapter 테스트")
class RedisFeedItemAdapterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RedisFeedItemAdapter adapter;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("피드 저장 시 ZSET에 추가한다")
    void save_AddsToZSet() {
        // Given
        FeedItem feedItem = FeedItem.of(
                null,
                new UserId(1L),
                ReviewId.of(10L),
                LocalDateTime.of(2024, 1, 1, 10, 0)
        );

        // When
        adapter.save(feedItem);

        // Then
        ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);
        verify(zSetOperations).add(eq("recommend:user:1"), eq("review:10"), scoreCaptor.capture());
        Double score = scoreCaptor.getValue();
        assertThat(score).isEqualTo(feedItem.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Test
    @DisplayName("피드 로드는 score를 createdAt으로 변환한다")
    void loadFeed_ParsesEntries() {
        // Given
        long score = 1000L;
        Set<ZSetOperations.TypedTuple<String>> entries = Set.of(
                new DefaultTypedTuple<>("review:1", (double) score),
                new DefaultTypedTuple<>("invalid", 2000.0)
        );
        when(zSetOperations.reverseRangeByScoreWithScores(eq("recommend:user:1"), eq(Double.POSITIVE_INFINITY), eq(Double.NEGATIVE_INFINITY), eq(0L), eq(10L)))
                .thenReturn(entries);

        // When
        var result = adapter.loadFeed(new UserId(1L), null, 10);

        // Then
        assertThat(result).hasSize(1);
        FeedItem item = result.get(0);
        assertThat(item.getReviewId().getValue()).isEqualTo(1L);
        assertThat(item.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(Instant.ofEpochMilli(score), ZoneOffset.UTC));
    }
}
