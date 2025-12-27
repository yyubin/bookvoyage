package org.yyubin.application.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.command.ToggleFollowCommand;
import org.yyubin.application.user.dto.ToggleFollowResult;
import org.yyubin.application.user.port.FollowPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 테스트")
class FollowServiceTest {

    @Mock
    private FollowPort followPort;

    @Mock
    private LoadUserPort loadUserPort;

    @InjectMocks
    private FollowService followService;

    private User followerUser;
    private User followeeUser;

    @BeforeEach
    void setUp() {
        followerUser = new User(
                new UserId(1L),
                "follower@example.com",
                "follower",
                "password",
                "Follower User",
                "Follower bio",
                "",
                Role.USER,
                AuthProvider.LOCAL,
                null,
                LocalDateTime.now()
        );

        followeeUser = new User(
                new UserId(2L),
                "followee@example.com",
                "followee",
                "password",
                "Followee User",
                "Followee bio",
                "",
                Role.USER,
                AuthProvider.LOCAL,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("팔로우 성공 - 아직 팔로우하지 않은 경우")
    void execute_FollowSuccess() {
        // Given
        ToggleFollowCommand command = new ToggleFollowCommand(1L, 2L);

        when(loadUserPort.loadById(new UserId(1L))).thenReturn(followerUser);
        when(loadUserPort.loadById(new UserId(2L))).thenReturn(followeeUser);
        when(followPort.exists(1L, 2L)).thenReturn(false);

        // When
        ToggleFollowResult result = followService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.following()).isTrue();

        verify(loadUserPort).loadById(new UserId(1L));
        verify(loadUserPort).loadById(new UserId(2L));
        verify(followPort).exists(1L, 2L);
        verify(followPort).create(1L, 2L);
        verify(followPort, never()).delete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("언팔로우 성공 - 이미 팔로우하고 있는 경우")
    void execute_UnfollowSuccess() {
        // Given
        ToggleFollowCommand command = new ToggleFollowCommand(1L, 2L);

        when(loadUserPort.loadById(new UserId(1L))).thenReturn(followerUser);
        when(loadUserPort.loadById(new UserId(2L))).thenReturn(followeeUser);
        when(followPort.exists(1L, 2L)).thenReturn(true);

        // When
        ToggleFollowResult result = followService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.following()).isFalse();

        verify(loadUserPort).loadById(new UserId(1L));
        verify(loadUserPort).loadById(new UserId(2L));
        verify(followPort).exists(1L, 2L);
        verify(followPort).delete(1L, 2L);
        verify(followPort, never()).create(anyLong(), anyLong());
    }

    @Test
    @DisplayName("자기 자신을 팔로우하려고 시도하면 예외 발생")
    void execute_FailWhenFollowingSelf() {
        // Given
        ToggleFollowCommand command = new ToggleFollowCommand(1L, 1L);

        // When & Then
        assertThatThrownBy(() -> followService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot follow yourself");

        verify(loadUserPort, never()).loadById(any());
        verify(followPort, never()).exists(anyLong(), anyLong());
        verify(followPort, never()).create(anyLong(), anyLong());
        verify(followPort, never()).delete(anyLong(), anyLong());
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 팔로우하려고 하면 예외 발생")
    void execute_FailWhenFollowerNotExists() {
        // Given
        ToggleFollowCommand command = new ToggleFollowCommand(1L, 2L);

        when(loadUserPort.loadById(new UserId(1L)))
                .thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        assertThatThrownBy(() -> followService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(loadUserPort).loadById(new UserId(1L));
        verify(followPort, never()).exists(anyLong(), anyLong());
    }

    @Test
    @DisplayName("팔로우 대상 사용자가 존재하지 않으면 예외 발생")
    void execute_FailWhenFolloweeNotExists() {
        // Given
        ToggleFollowCommand command = new ToggleFollowCommand(1L, 2L);

        when(loadUserPort.loadById(new UserId(1L))).thenReturn(followerUser);
        when(loadUserPort.loadById(new UserId(2L)))
                .thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        assertThatThrownBy(() -> followService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(loadUserPort).loadById(new UserId(1L));
        verify(loadUserPort).loadById(new UserId(2L));
        verify(followPort, never()).exists(anyLong(), anyLong());
    }
}
