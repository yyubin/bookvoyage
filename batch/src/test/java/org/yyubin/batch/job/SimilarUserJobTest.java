package org.yyubin.batch.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.usecase.FindSimilarUsersUseCase;
import org.yyubin.domain.recommendation.UserTasteVector;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimilarUserJob 테스트")
class SimilarUserJobTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private ReviewCircleCachePort cachePort;

    @Mock
    private FindSimilarUsersUseCase findSimilarUsersUseCase;

    @InjectMocks
    private SimilarUserJob similarUserJob;

    @Test
    @DisplayName("유사 사용자 계산 성공")
    void findSimilarUsers_Success() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        UserTasteVector vector1 = createTasteVector(1L);
        UserTasteVector vector2 = createTasteVector(2L);
        when(cachePort.getTasteVector(1L)).thenReturn(Optional.of(vector1));
        when(cachePort.getTasteVector(2L)).thenReturn(Optional.of(vector2));

        // When
        similarUserJob.findSimilarUsers();

        // Then
        verify(userRepository).findAll();
        verify(cachePort).getTasteVector(1L);
        verify(cachePort).getTasteVector(2L);
        verify(findSimilarUsersUseCase, times(2)).execute(anyLong(), anyList());
    }

    @Test
    @DisplayName("사용자가 없는 경우")
    void findSimilarUsers_NoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        similarUserJob.findSimilarUsers();

        // Then
        verify(userRepository).findAll();
        verify(findSimilarUsersUseCase, never()).execute(anyLong(), anyList());
    }

    @Test
    @DisplayName("취향 벡터가 없는 경우 스킵")
    void findSimilarUsers_NoTasteVectors() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(cachePort.getTasteVector(anyLong())).thenReturn(Optional.empty());

        // When
        similarUserJob.findSimilarUsers();

        // Then
        verify(userRepository).findAll();
        verify(findSimilarUsersUseCase, never()).execute(anyLong(), anyList());
    }

    @Test
    @DisplayName("일부 사용자에게만 취향 벡터가 있는 경우")
    void findSimilarUsers_PartialTasteVectors() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        UserEntity user3 = createUserEntity(3L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        UserTasteVector vector1 = createTasteVector(1L);
        when(cachePort.getTasteVector(1L)).thenReturn(Optional.of(vector1));
        when(cachePort.getTasteVector(2L)).thenReturn(Optional.empty());
        when(cachePort.getTasteVector(3L)).thenReturn(Optional.empty());

        // When
        similarUserJob.findSimilarUsers();

        // Then
        verify(findSimilarUsersUseCase, times(1)).execute(eq(1L), anyList());
    }

    @Test
    @DisplayName("일부 사용자 처리 실패 시 나머지 계속 처리")
    void findSimilarUsers_PartialFailure() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        UserTasteVector vector1 = createTasteVector(1L);
        UserTasteVector vector2 = createTasteVector(2L);
        when(cachePort.getTasteVector(1L)).thenReturn(Optional.of(vector1));
        when(cachePort.getTasteVector(2L)).thenReturn(Optional.of(vector2));

        doThrow(new RuntimeException("Calculation failed")).when(findSimilarUsersUseCase).execute(eq(1L), anyList());

        // When
        similarUserJob.findSimilarUsers();

        // Then
        verify(findSimilarUsersUseCase).execute(eq(1L), anyList());
        verify(findSimilarUsersUseCase).execute(eq(2L), anyList());
    }

    @Test
    @DisplayName("사용자 조회 실패 시 예외 전파")
    void findSimilarUsers_UserQueryFails() {
        // Given
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> similarUserJob.findSimilarUsers())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(findSimilarUsersUseCase, never()).execute(anyLong(), anyList());
    }

    @Test
    @DisplayName("대량 사용자 처리 성공")
    void findSimilarUsers_LargeUserCount() {
        // Given
        List<UserEntity> users = java.util.stream.LongStream.rangeClosed(1, 150)
                .mapToObj(this::createUserEntity)
                .toList();
        when(userRepository.findAll()).thenReturn(users);

        for (long i = 1; i <= 150; i++) {
            when(cachePort.getTasteVector(i)).thenReturn(Optional.of(createTasteVector(i)));
        }

        // When
        similarUserJob.findSimilarUsers();

        // Then
        verify(findSimilarUsersUseCase, times(150)).execute(anyLong(), anyList());
    }

    private UserEntity createUserEntity(Long id) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private UserTasteVector createTasteVector(Long userId) {
        return new UserTasteVector(
                userId,
                Map.of("FANTASY", 0.5, "ROMANCE", 0.3, "magic", 0.4, "love", 0.3),
                LocalDateTime.now()
        );
    }
}
