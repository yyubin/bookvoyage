package org.yyubin.batch.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.recommendation.usecase.BuildUserTasteVectorUseCase;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserTasteProfileJob 테스트")
class UserTasteProfileJobTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private BuildUserTasteVectorUseCase buildUserTasteVectorUseCase;

    @InjectMocks
    private UserTasteProfileJob userTasteProfileJob;

    @Test
    @DisplayName("모든 사용자의 취향 벡터 생성 성공")
    void buildUserTasteVectors_Success() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        UserEntity user3 = createUserEntity(3L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        // When
        userTasteProfileJob.buildUserTasteVectors();

        // Then
        verify(userRepository).findAll();
        verify(buildUserTasteVectorUseCase).execute(1L);
        verify(buildUserTasteVectorUseCase).execute(2L);
        verify(buildUserTasteVectorUseCase).execute(3L);
        verify(buildUserTasteVectorUseCase, times(3)).execute(anyLong());
    }

    @Test
    @DisplayName("사용자가 없는 경우")
    void buildUserTasteVectors_NoUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        userTasteProfileJob.buildUserTasteVectors();

        // Then
        verify(userRepository).findAll();
        verify(buildUserTasteVectorUseCase, never()).execute(anyLong());
    }

    @Test
    @DisplayName("일부 사용자 처리 실패 시 나머지 계속 처리")
    void buildUserTasteVectors_PartialFailure() {
        // Given
        UserEntity user1 = createUserEntity(1L);
        UserEntity user2 = createUserEntity(2L);
        UserEntity user3 = createUserEntity(3L);
        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        doThrow(new RuntimeException("Vector build failed")).when(buildUserTasteVectorUseCase).execute(2L);

        // When
        userTasteProfileJob.buildUserTasteVectors();

        // Then
        verify(buildUserTasteVectorUseCase).execute(1L);
        verify(buildUserTasteVectorUseCase).execute(2L);
        verify(buildUserTasteVectorUseCase).execute(3L);
    }

    @Test
    @DisplayName("사용자 조회 실패 시 예외 전파")
    void buildUserTasteVectors_UserQueryFails() {
        // Given
        when(userRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> userTasteProfileJob.buildUserTasteVectors())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(buildUserTasteVectorUseCase, never()).execute(anyLong());
    }

    @Test
    @DisplayName("대량 사용자 처리 성공")
    void buildUserTasteVectors_LargeUserCount() {
        // Given
        List<UserEntity> users = java.util.stream.LongStream.rangeClosed(1, 150)
                .mapToObj(this::createUserEntity)
                .toList();
        when(userRepository.findAll()).thenReturn(users);

        // When
        userTasteProfileJob.buildUserTasteVectors();

        // Then
        verify(buildUserTasteVectorUseCase, times(150)).execute(anyLong());
    }

    private UserEntity createUserEntity(Long id) {
        UserEntity user = mock(UserEntity.class);
        when(user.getId()).thenReturn(id);
        return user;
    }
}
