package org.yyubin.recommendation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRecommendationExposureService 테스트")
class ReviewRecommendationExposureServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ReviewRecommendationProperties properties;

    @Mock
    private ReviewRecommendationProperties.Search searchConfig;

    @InjectMocks
    private ReviewRecommendationExposureService exposureService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Nested
    @DisplayName("loadRecentReviewIds 테스트")
    class LoadRecentReviewIdsTest {

        @Test
        @DisplayName("최근 노출된 리뷰 ID를 조회한다")
        void load_Success() {
            // Given
            Long userId = 1L;
            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureFilterLimit()).thenReturn(200);

            Set<String> rawValues = new LinkedHashSet<>();
            rawValues.add("100");
            rawValues.add("101");
            rawValues.add("102");

            when(zSetOperations.reverseRange("recommend:review:exposed:user:1", 0, 199))
                    .thenReturn(rawValues);

            // When
            Set<Long> result = exposureService.loadRecentReviewIds(userId);

            // Then
            assertThat(result).containsExactly(100L, 101L, 102L);
        }

        @Test
        @DisplayName("null userId는 빈 Set을 반환한다")
        void load_NullUserId_ReturnsEmptySet() {
            // When
            Set<Long> result = exposureService.loadRecentReviewIds(null);

            // Then
            assertThat(result).isEmpty();
            verify(zSetOperations, never()).reverseRange(anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("빈 캐시는 빈 Set을 반환한다")
        void load_EmptyCache_ReturnsEmptySet() {
            // Given
            Long userId = 1L;
            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureFilterLimit()).thenReturn(200);
            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong()))
                    .thenReturn(null);

            // When
            Set<Long> result = exposureService.loadRecentReviewIds(userId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 캐시는 빈 Set을 반환한다")
        void load_NullResult_ReturnsEmptySet() {
            // Given
            Long userId = 1L;
            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureFilterLimit()).thenReturn(200);
            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong()))
                    .thenReturn(Set.of());

            // When
            Set<Long> result = exposureService.loadRecentReviewIds(userId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("파싱 불가능한 값은 무시한다")
        void load_InvalidValues_Ignored() {
            // Given
            Long userId = 1L;
            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureFilterLimit()).thenReturn(200);

            Set<String> rawValues = new LinkedHashSet<>();
            rawValues.add("100");
            rawValues.add("invalid");
            rawValues.add("102");

            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong()))
                    .thenReturn(rawValues);

            // When
            Set<Long> result = exposureService.loadRecentReviewIds(userId);

            // Then
            assertThat(result).containsExactly(100L, 102L);
        }

        @Test
        @DisplayName("null 값은 무시한다")
        void load_NullValues_Ignored() {
            // Given
            Long userId = 1L;
            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureFilterLimit()).thenReturn(200);

            Set<String> rawValues = new LinkedHashSet<>();
            rawValues.add("100");
            rawValues.add(null);
            rawValues.add("102");

            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong()))
                    .thenReturn(rawValues);

            // When
            Set<Long> result = exposureService.loadRecentReviewIds(userId);

            // Then
            assertThat(result).containsExactly(100L, 102L);
        }

        @Test
        @DisplayName("예외 발생시 빈 Set을 반환한다")
        void load_Exception_ReturnsEmptySet() {
            // Given
            Long userId = 1L;
            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureFilterLimit()).thenReturn(200);
            when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Redis error"));

            // When
            Set<Long> result = exposureService.loadRecentReviewIds(userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("recordExposure 테스트")
    class RecordExposureTest {

        @Test
        @DisplayName("노출된 리뷰 ID를 기록한다")
        void record_Success() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = List.of(100L, 101L, 102L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(searchConfig.getExposureTtlHours()).thenReturn(24);
            when(zSetOperations.size(anyString())).thenReturn(3L);

            // When
            exposureService.recordExposure(userId, reviewIds);

            // Then
            verify(zSetOperations, times(3)).add(eq("recommend:review:exposed:user:1"), anyString(), anyDouble());
            verify(redisTemplate).expire(eq("recommend:review:exposed:user:1"), eq(24L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("null userId는 아무 작업도 하지 않는다")
        void record_NullUserId_NoOp() {
            // When
            exposureService.recordExposure(null, List.of(100L));

            // Then
            verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("null reviewIds는 아무 작업도 하지 않는다")
        void record_NullReviewIds_NoOp() {
            // When
            exposureService.recordExposure(1L, null);

            // Then
            verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("빈 reviewIds는 아무 작업도 하지 않는다")
        void record_EmptyReviewIds_NoOp() {
            // When
            exposureService.recordExposure(1L, List.of());

            // Then
            verify(zSetOperations, never()).add(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("null reviewId는 저장하지 않는다")
        void record_NullReviewId_Skipped() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = new java.util.ArrayList<>();
            reviewIds.add(100L);
            reviewIds.add(null);
            reviewIds.add(102L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(searchConfig.getExposureTtlHours()).thenReturn(24);
            when(zSetOperations.size(anyString())).thenReturn(2L);

            // When
            exposureService.recordExposure(userId, reviewIds);

            // Then
            verify(zSetOperations, times(2)).add(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("maxItems 초과시 하위 항목을 제거한다")
        void record_TrimsExcess() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = List.of(100L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(searchConfig.getExposureTtlHours()).thenReturn(24);
            when(zSetOperations.size(anyString())).thenReturn(250L);

            // When
            exposureService.recordExposure(userId, reviewIds);

            // Then
            verify(zSetOperations).removeRange(eq("recommend:review:exposed:user:1"), eq(0L), eq(49L));
        }

        @Test
        @DisplayName("maxItems 이하면 trim하지 않는다")
        void record_NoTrimWhenUnderLimit() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = List.of(100L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(searchConfig.getExposureTtlHours()).thenReturn(24);
            when(zSetOperations.size(anyString())).thenReturn(100L);

            // When
            exposureService.recordExposure(userId, reviewIds);

            // Then
            verify(zSetOperations, never()).removeRange(anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("null size는 trim하지 않는다")
        void record_NullSize_NoTrim() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = List.of(100L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(searchConfig.getExposureTtlHours()).thenReturn(24);
            when(zSetOperations.size(anyString())).thenReturn(null);

            // When
            exposureService.recordExposure(userId, reviewIds);

            // Then
            verify(zSetOperations, never()).removeRange(anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("예외 발생시 로그만 남기고 실패하지 않는다")
        void record_Exception_NoThrow() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = List.of(100L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(zSetOperations.add(anyString(), anyString(), anyDouble()))
                    .thenThrow(new RuntimeException("Redis error"));

            // When & Then (no exception thrown)
            exposureService.recordExposure(userId, reviewIds);
        }

        @Test
        @DisplayName("현재 시간을 score로 사용한다")
        void record_UsesCurrentTimeAsScore() {
            // Given
            Long userId = 1L;
            List<Long> reviewIds = List.of(100L);

            when(properties.getSearch()).thenReturn(searchConfig);
            when(searchConfig.getExposureMaxItems()).thenReturn(200);
            when(searchConfig.getExposureTtlHours()).thenReturn(24);
            when(zSetOperations.size(anyString())).thenReturn(1L);

            long beforeTime = System.currentTimeMillis();

            // When
            exposureService.recordExposure(userId, reviewIds);

            long afterTime = System.currentTimeMillis();

            // Then
            verify(zSetOperations).add(
                    eq("recommend:review:exposed:user:1"),
                    eq("100"),
                    doubleThat(score -> score >= beforeTime && score <= afterTime)
            );
        }
    }
}
