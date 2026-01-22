package org.yyubin.application.user.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetUserProfileQueryHandler 테스트")
class GetUserProfileQueryHandlerTest {

    @Mock
    private LoadUserPort loadUserPort;

    @InjectMocks
    private GetUserProfileQueryHandler handler;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                new UserId(1L),
                "test@example.com",
                "testuser",
                "password123",
                "테스트닉네임",
                "안녕하세요. 테스트 유저입니다.",
                "소설,에세이",
                Role.USER,
                AuthProvider.LOCAL,
                "http://example.com/profile.jpg",
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("handle 메서드")
    class HandleMethod {

        @Test
        @DisplayName("사용자 프로필 조회 성공")
        void handle_Success() {
            // Given
            GetUserProfileQuery query = new GetUserProfileQuery(1L);
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            // When
            UserProfileResult result = handler.handle(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.nickname()).isEqualTo("테스트닉네임");
            assertThat(result.bio()).isEqualTo("안녕하세요. 테스트 유저입니다.");
            assertThat(result.tasteTag()).isEqualTo("소설,에세이");
            assertThat(result.provider()).isEqualTo("LOCAL");
            assertThat(result.profileImageUrl()).isEqualTo("http://example.com/profile.jpg");

            verify(loadUserPort).loadById(new UserId(1L));
        }

        @Test
        @DisplayName("OAuth2 사용자 프로필 조회 성공")
        void handle_Success_OAuth2User() {
            // Given
            User oauthUser = new User(
                    new UserId(2L),
                    "oauth@example.com",
                    "oauthuser",
                    "",
                    "OAuth닉네임",
                    "OAuth 유저입니다.",
                    "자기계발",
                    Role.USER,
                    AuthProvider.GOOGLE,
                    "http://google.com/profile.jpg",
                    LocalDateTime.now()
            );
            GetUserProfileQuery query = new GetUserProfileQuery(2L);
            when(loadUserPort.loadById(new UserId(2L))).thenReturn(oauthUser);

            // When
            UserProfileResult result = handler.handle(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(2L);
            assertThat(result.provider()).isEqualTo("GOOGLE");

            verify(loadUserPort).loadById(new UserId(2L));
        }

        @Test
        @DisplayName("프로필 이미지가 null인 경우")
        void handle_Success_NullProfileImage() {
            // Given
            User userWithoutImage = new User(
                    new UserId(3L),
                    "noimage@example.com",
                    "noimageuser",
                    "password123",
                    "노이미지",
                    "",
                    "",
                    Role.USER,
                    AuthProvider.LOCAL,
                    null,
                    LocalDateTime.now()
            );
            GetUserProfileQuery query = new GetUserProfileQuery(3L);
            when(loadUserPort.loadById(new UserId(3L))).thenReturn(userWithoutImage);

            // When
            UserProfileResult result = handler.handle(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.profileImageUrl()).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void handle_ThrowsExceptionWhenUserNotFound() {
            // Given
            GetUserProfileQuery query = new GetUserProfileQuery(999L);
            when(loadUserPort.loadById(new UserId(999L)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(query))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");

            verify(loadUserPort).loadById(new UserId(999L));
        }
    }

    @Nested
    @DisplayName("GetUserProfileQuery 유효성 검사")
    class QueryValidation {

        @Test
        @DisplayName("null userId로 쿼리 생성 시 예외 발생")
        void query_ThrowsExceptionForNullUserId() {
            assertThatThrownBy(() -> new GetUserProfileQuery(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");
        }

        @Test
        @DisplayName("0 이하의 userId로 쿼리 생성 시 예외 발생")
        void query_ThrowsExceptionForInvalidUserId() {
            assertThatThrownBy(() -> new GetUserProfileQuery(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");

            assertThatThrownBy(() -> new GetUserProfileQuery(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");
        }

        @Test
        @DisplayName("유효한 userId로 쿼리 생성 성공")
        void query_Success() {
            GetUserProfileQuery query = new GetUserProfileQuery(1L);
            assertThat(query.userId()).isEqualTo(1L);
        }
    }
}
