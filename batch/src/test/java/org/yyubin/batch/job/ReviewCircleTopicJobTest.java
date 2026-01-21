package org.yyubin.batch.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.usecase.AggregateReviewCircleTopicsUseCase;
import org.yyubin.domain.recommendation.SimilarUser;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewCircleTopicJob 테스트")
class ReviewCircleTopicJobTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ReviewCircleCachePort cachePort;

    @Mock
    private AggregateReviewCircleTopicsUseCase aggregateTopicsUseCase;

    @InjectMocks
    private ReviewCircleTopicJob reviewCircleTopicJob;

    @Test
    @DisplayName("리뷰 서클 토픽 집계 성공")
    void aggregateReviewCircleTopics_Success() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(cachePort.getSimilarUsers(anyLong())).thenReturn(List.of(
                SimilarUser.of(100L, 0.9),
                SimilarUser.of(101L, 0.8)
        ));

        // When
        reviewCircleTopicJob.aggregateReviewCircleTopics();

        // Then
        verify(userRepository).findAll();
        // 2 users × 2 windows = 4 times
        verify(aggregateTopicsUseCase, times(4)).execute(anyLong(), anyString());
    }

    @Test
    @DisplayName("사용자가 없는 경우")
    void aggregateReviewCircleTopics_NoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        reviewCircleTopicJob.aggregateReviewCircleTopics();

        // Then
        verify(userRepository).findAll();
        verify(aggregateTopicsUseCase, never()).execute(anyLong(), anyString());
    }

    @Test
    @DisplayName("유사 사용자가 없는 경우 스킵")
    void aggregateReviewCircleTopics_NoSimilarUsers() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        when(userRepository.findAll()).thenReturn(List.of(user1));
        when(cachePort.getSimilarUsers(1L)).thenReturn(Collections.emptyList());

        // When
        reviewCircleTopicJob.aggregateReviewCircleTopics();

        // Then
        verify(cachePort, times(2)).getSimilarUsers(1L); // 2 windows
        verify(aggregateTopicsUseCase, never()).execute(anyLong(), anyString());
    }

    @Test
    @DisplayName("일부 사용자 처리 실패 시 나머지 계속 처리")
    void aggregateReviewCircleTopics_PartialFailure() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(cachePort.getSimilarUsers(anyLong())).thenReturn(List.of(SimilarUser.of(100L, 0.9)));

        doThrow(new RuntimeException("Aggregation failed")).when(aggregateTopicsUseCase).execute(eq(1L), anyString());

        // When
        reviewCircleTopicJob.aggregateReviewCircleTopics();

        // Then
        verify(aggregateTopicsUseCase, times(2)).execute(eq(1L), anyString());
        verify(aggregateTopicsUseCase, times(2)).execute(eq(2L), anyString());
    }

    @Test
    @DisplayName("사용자 조회 실패 시 예외 전파")
    void aggregateReviewCircleTopics_UserQueryFails() {
        // Given
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> reviewCircleTopicJob.aggregateReviewCircleTopics())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(aggregateTopicsUseCase, never()).execute(anyLong(), anyString());
    }

    @Test
    @DisplayName("두 개의 윈도우(24h, 7d)에 대해 처리")
    void aggregateReviewCircleTopics_ProcessesBothWindows() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        when(userRepository.findAll()).thenReturn(List.of(user1));
        when(cachePort.getSimilarUsers(1L)).thenReturn(List.of(SimilarUser.of(100L, 0.9)));

        // When
        reviewCircleTopicJob.aggregateReviewCircleTopics();

        // Then
        verify(aggregateTopicsUseCase).execute(1L, "24h");
        verify(aggregateTopicsUseCase).execute(1L, "7d");
    }

    private UserEntity createUserEntity(Long id) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}
