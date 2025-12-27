package org.yyubin.application.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.dto.FollowCountResult;
import org.yyubin.application.user.dto.FollowPageResult;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.user.query.GetFollowCountQuery;
import org.yyubin.application.user.query.GetFollowerUsersQuery;
import org.yyubin.application.user.query.GetFollowingUsersQuery;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowQueryService 테스트")
class FollowQueryServiceTest {

    @Mock
    private FollowQueryPort followQueryPort;

    @Mock
    private LoadUserPort loadUserPort;

    @InjectMocks
    private FollowQueryService followQueryService;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        testUser1 = new User(
                new UserId(1L),
                "user1@test.com",
                "user1",
                "password123",
                "User One",
                "",
                "",
                null,
                null,
                "http://example.com/profile1.jpg",
                null
        );

        testUser2 = new User(
                new UserId(2L),
                "user2@test.com",
                "user2",
                "password123",
                "User Two",
                "",
                "",
                null,
                null,
                "http://example.com/profile2.jpg",
                null
        );

        testUser3 = new User(
                new UserId(3L),
                "user3@test.com",
                "user3",
                "password123",
                "User Three",
                "",
                "",
                null,
                null,
                "http://example.com/profile3.jpg",
                null
        );
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 다음 페이지 있음")
    void getFollowing_Success_WithNextPage() {
        // Given
        GetFollowingUsersQuery query = new GetFollowingUsersQuery(1L, null, 2);

        when(followQueryPort.loadFollowingIds(1L, null, 3))
                .thenReturn(List.of(2L, 3L, 4L));
        when(loadUserPort.loadById(new UserId(2L))).thenReturn(testUser1);
        when(loadUserPort.loadById(new UserId(3L))).thenReturn(testUser2);

        // When
        FollowPageResult result = followQueryService.getFollowing(query);

        // Then
        assertThat(result.users()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(4L);

        verify(followQueryPort).loadFollowingIds(1L, null, 3);
        verify(loadUserPort, times(2)).loadById(any(UserId.class));
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 다음 페이지 없음")
    void getFollowing_Success_NoNextPage() {
        // Given
        GetFollowingUsersQuery query = new GetFollowingUsersQuery(1L, null, 10);

        when(followQueryPort.loadFollowingIds(1L, null, 11))
                .thenReturn(List.of(2L, 3L));
        when(loadUserPort.loadById(new UserId(2L))).thenReturn(testUser1);
        when(loadUserPort.loadById(new UserId(3L))).thenReturn(testUser2);

        // When
        FollowPageResult result = followQueryService.getFollowing(query);

        // Then
        assertThat(result.users()).hasSize(2);
        assertThat(result.nextCursor()).isNull();

        verify(followQueryPort).loadFollowingIds(1L, null, 11);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 빈 결과")
    void getFollowing_Success_EmptyResult() {
        // Given
        GetFollowingUsersQuery query = new GetFollowingUsersQuery(1L, null, 10);

        when(followQueryPort.loadFollowingIds(1L, null, 11))
                .thenReturn(List.of());

        // When
        FollowPageResult result = followQueryService.getFollowing(query);

        // Then
        assertThat(result.users()).isEmpty();
        assertThat(result.nextCursor()).isNull();

        verify(followQueryPort).loadFollowingIds(1L, null, 11);
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공 - 커서 기반 페이지네이션")
    void getFollowing_Success_WithCursor() {
        // Given
        GetFollowingUsersQuery query = new GetFollowingUsersQuery(1L, 5L, 5);

        when(followQueryPort.loadFollowingIds(1L, 5L, 6))
                .thenReturn(List.of(6L));
        when(loadUserPort.loadById(new UserId(6L))).thenReturn(testUser1);

        // When
        FollowPageResult result = followQueryService.getFollowing(query);

        // Then
        assertThat(result.users()).hasSize(1);
        assertThat(result.nextCursor()).isNull();

        verify(followQueryPort).loadFollowingIds(1L, 5L, 6);
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - 다음 페이지 있음")
    void getFollowers_Success_WithNextPage() {
        // Given
        GetFollowerUsersQuery query = new GetFollowerUsersQuery(1L, null, 2);

        when(followQueryPort.loadFollowerIds(1L, null, 3))
                .thenReturn(List.of(2L, 3L, 4L));
        when(loadUserPort.loadById(new UserId(2L))).thenReturn(testUser1);
        when(loadUserPort.loadById(new UserId(3L))).thenReturn(testUser2);

        // When
        FollowPageResult result = followQueryService.getFollowers(query);

        // Then
        assertThat(result.users()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(4L);

        verify(followQueryPort).loadFollowerIds(1L, null, 3);
        verify(loadUserPort, times(2)).loadById(any(UserId.class));
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - 다음 페이지 없음")
    void getFollowers_Success_NoNextPage() {
        // Given
        GetFollowerUsersQuery query = new GetFollowerUsersQuery(1L, null, 10);

        when(followQueryPort.loadFollowerIds(1L, null, 11))
                .thenReturn(List.of(2L));
        when(loadUserPort.loadById(new UserId(2L))).thenReturn(testUser1);

        // When
        FollowPageResult result = followQueryService.getFollowers(query);

        // Then
        assertThat(result.users()).hasSize(1);
        assertThat(result.nextCursor()).isNull();

        verify(followQueryPort).loadFollowerIds(1L, null, 11);
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공 - 빈 결과")
    void getFollowers_Success_EmptyResult() {
        // Given
        GetFollowerUsersQuery query = new GetFollowerUsersQuery(1L, null, 10);

        when(followQueryPort.loadFollowerIds(1L, null, 11))
                .thenReturn(List.of());

        // When
        FollowPageResult result = followQueryService.getFollowers(query);

        // Then
        assertThat(result.users()).isEmpty();
        assertThat(result.nextCursor()).isNull();

        verify(followQueryPort).loadFollowerIds(1L, null, 11);
    }

    @Test
    @DisplayName("팔로우 수 조회 성공")
    void getCounts_Success() {
        // Given
        GetFollowCountQuery query = new GetFollowCountQuery(1L);

        when(followQueryPort.countFollowing(1L)).thenReturn(10L);
        when(followQueryPort.countFollowers(1L)).thenReturn(5L);

        // When
        FollowCountResult result = followQueryService.getCounts(query);

        // Then
        assertThat(result.followingCount()).isEqualTo(10L);
        assertThat(result.followerCount()).isEqualTo(5L);

        verify(followQueryPort).countFollowing(1L);
        verify(followQueryPort).countFollowers(1L);
    }

    @Test
    @DisplayName("팔로우 수 조회 성공 - 팔로우 없음")
    void getCounts_Success_NoFollows() {
        // Given
        GetFollowCountQuery query = new GetFollowCountQuery(1L);

        when(followQueryPort.countFollowing(1L)).thenReturn(0L);
        when(followQueryPort.countFollowers(1L)).thenReturn(0L);

        // When
        FollowCountResult result = followQueryService.getCounts(query);

        // Then
        assertThat(result.followingCount()).isEqualTo(0L);
        assertThat(result.followerCount()).isEqualTo(0L);

        verify(followQueryPort).countFollowing(1L);
        verify(followQueryPort).countFollowers(1L);
    }
}
