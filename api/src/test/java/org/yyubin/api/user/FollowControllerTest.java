package org.yyubin.api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.yyubin.application.user.*;
import org.yyubin.application.user.dto.FollowCountResult;
import org.yyubin.application.user.dto.FollowPageResult;
import org.yyubin.application.user.dto.FollowUserView;
import org.yyubin.application.user.dto.ToggleFollowResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@DisplayName("FollowController 테스트")
class FollowControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ToggleFollowUseCase toggleFollowUseCase;

    @MockitoBean
    private GetFollowingUsersUseCase getFollowingUsersUseCase;

    @MockitoBean
    private GetFollowerUsersUseCase getFollowerUsersUseCase;

    @MockitoBean
    private GetFollowCountUseCase getFollowCountUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("팔로우 토글 - 팔로우 성공")
    @WithMockUser(username = "1")
    void toggleFollow_Follow() throws Exception {
        // Given
        Long targetUserId = 2L;
        ToggleFollowResult result = new ToggleFollowResult(true);
        when(toggleFollowUseCase.execute(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/users/{targetUserId}/follow", targetUserId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));

        verify(toggleFollowUseCase).execute(any());
    }

    @Test
    @DisplayName("팔로우 토글 - 언팔로우 성공")
    @WithMockUser(username = "1")
    void toggleFollow_Unfollow() throws Exception {
        // Given
        Long targetUserId = 2L;
        ToggleFollowResult result = new ToggleFollowResult(false);
        when(toggleFollowUseCase.execute(any())).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/users/{targetUserId}/follow", targetUserId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));

        verify(toggleFollowUseCase).execute(any());
    }

    @Test
    @DisplayName("팔로잉 목록 조회 성공")
    void getFollowing_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<FollowUserView> users = List.of(
                new FollowUserView(2L, "user2", "User 2", "avatar2.jpg"),
                new FollowUserView(3L, "user3", "User 3", "avatar3.jpg")
        );
        FollowPageResult pageResult = new FollowPageResult(users, 4L);
        when(getFollowingUsersUseCase.getFollowing(any())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/following", userId)
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].userId").value(2))
                .andExpect(jsonPath("$.users[0].username").value("user2"))
                .andExpect(jsonPath("$.users[1].userId").value(3))
                .andExpect(jsonPath("$.nextCursor").value(4));

        verify(getFollowingUsersUseCase).getFollowing(any());
    }

    @Test
    @DisplayName("팔로워 목록 조회 성공")
    void getFollowers_Success() throws Exception {
        // Given
        Long userId = 1L;
        List<FollowUserView> users = List.of(
                new FollowUserView(4L, "user4", "User 4", "avatar4.jpg")
        );
        FollowPageResult pageResult = new FollowPageResult(users, null);
        when(getFollowerUsersUseCase.getFollowers(any())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/followers", userId)
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].userId").value(4));

        verify(getFollowerUsersUseCase).getFollowers(any());
    }

    @Test
    @DisplayName("팔로잉 수 조회 성공")
    void getFollowingCount_Success() throws Exception {
        // Given
        Long userId = 1L;
        FollowCountResult countResult = new FollowCountResult(50, 30);
        when(getFollowCountUseCase.getCounts(any())).thenReturn(countResult);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/following/count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(50));

        verify(getFollowCountUseCase).getCounts(any());
    }

    @Test
    @DisplayName("팔로워 수 조회 성공")
    void getFollowersCount_Success() throws Exception {
        // Given
        Long userId = 1L;
        FollowCountResult countResult = new FollowCountResult(50, 30);
        when(getFollowCountUseCase.getCounts(any())).thenReturn(countResult);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}/followers/count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(30));

        verify(getFollowCountUseCase).getCounts(any());
    }
}
