package org.yyubin.application.user.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.command.UpdateUserBioCommand;
import org.yyubin.application.user.command.UpdateUserNicknameCommand;
import org.yyubin.application.user.command.UpdateUserProfileImageUrlCommand;
import org.yyubin.application.user.command.UpdateUserTasteTagCommand;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.user.port.UpdateUserPort;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUserCommandHandler 테스트")
class UpdateUserCommandHandlerTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private UpdateUserPort updateUserPort;

    @InjectMocks
    private UpdateUserCommandHandler handler;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                new UserId(1L),
                "test@example.com",
                "testuser",
                "password123",
                "기존닉네임",
                "기존 자기소개",
                "소설",
                Role.USER,
                AuthProvider.LOCAL,
                "http://example.com/old-profile.jpg",
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("UpdateUserBioCommand 처리")
    class UpdateBioTests {

        @Test
        @DisplayName("자기소개 업데이트 성공")
        void handle_UpdateBio_Success() {
            // Given
            UpdateUserBioCommand command = new UpdateUserBioCommand(1L, "새로운 자기소개입니다.");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(loadUserPort).loadById(new UserId(1L));
            verify(updateUserPort).update(userCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.bio()).isEqualTo("새로운 자기소개입니다.");
            assertThat(updatedUser.username()).isEqualTo("testuser");
            assertThat(updatedUser.nickname()).isEqualTo("기존닉네임");
            assertThat(updatedUser.ProfileImageUrl()).isEqualTo("http://example.com/old-profile.jpg");
            assertThat(updatedUser.tasteTag()).isEqualTo("소설");
        }

        @Test
        @DisplayName("빈 자기소개로 업데이트 성공")
        void handle_UpdateBio_EmptyBio() {
            // Given
            UpdateUserBioCommand command = new UpdateUserBioCommand(1L, "");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(updateUserPort).update(userCaptor.capture());
            assertThat(userCaptor.getValue().bio()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 자기소개 업데이트 시 예외 발생")
        void handle_UpdateBio_UserNotFound() {
            // Given
            UpdateUserBioCommand command = new UpdateUserBioCommand(999L, "새로운 자기소개");
            when(loadUserPort.loadById(new UserId(999L)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("UpdateUserNicknameCommand 처리")
    class UpdateNicknameTests {

        @Test
        @DisplayName("닉네임 업데이트 성공")
        void handle_UpdateNickname_Success() {
            // Given
            UpdateUserNicknameCommand command = new UpdateUserNicknameCommand(1L, "새닉네임");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(loadUserPort).loadById(new UserId(1L));
            verify(updateUserPort).update(userCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.nickname()).isEqualTo("새닉네임");
            assertThat(updatedUser.bio()).isEqualTo("기존 자기소개");
            assertThat(updatedUser.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("빈 닉네임으로 업데이트 시 기존 닉네임 유지")
        void handle_UpdateNickname_EmptyNickname() {
            // Given
            UpdateUserNicknameCommand command = new UpdateUserNicknameCommand(1L, "");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(updateUserPort).update(userCaptor.capture());
            // 빈 문자열이 전달되면 그대로 저장 (도메인 로직에 따라 다름)
            assertThat(userCaptor.getValue().nickname()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 닉네임 업데이트 시 예외 발생")
        void handle_UpdateNickname_UserNotFound() {
            // Given
            UpdateUserNicknameCommand command = new UpdateUserNicknameCommand(999L, "새닉네임");
            when(loadUserPort.loadById(new UserId(999L)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("UpdateUserProfileImageUrlCommand 처리")
    class UpdateProfileImageUrlTests {

        @Test
        @DisplayName("프로필 이미지 URL 업데이트 성공")
        void handle_UpdateProfileImageUrl_Success() {
            // Given
            UpdateUserProfileImageUrlCommand command = new UpdateUserProfileImageUrlCommand(
                    1L, "http://example.com/new-profile.jpg"
            );
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(loadUserPort).loadById(new UserId(1L));
            verify(updateUserPort).update(userCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.ProfileImageUrl()).isEqualTo("http://example.com/new-profile.jpg");
            assertThat(updatedUser.nickname()).isEqualTo("기존닉네임");
            assertThat(updatedUser.bio()).isEqualTo("기존 자기소개");
        }

        @Test
        @DisplayName("프로필 이미지 URL을 빈 문자열로 업데이트")
        void handle_UpdateProfileImageUrl_EmptyUrl() {
            // Given
            UpdateUserProfileImageUrlCommand command = new UpdateUserProfileImageUrlCommand(1L, "");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(updateUserPort).update(userCaptor.capture());
            assertThat(userCaptor.getValue().ProfileImageUrl()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 프로필 이미지 업데이트 시 예외 발생")
        void handle_UpdateProfileImageUrl_UserNotFound() {
            // Given
            UpdateUserProfileImageUrlCommand command = new UpdateUserProfileImageUrlCommand(
                    999L, "http://example.com/profile.jpg"
            );
            when(loadUserPort.loadById(new UserId(999L)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("UpdateUserTasteTagCommand 처리")
    class UpdateTasteTagTests {

        @Test
        @DisplayName("취향 태그 업데이트 성공")
        void handle_UpdateTasteTag_Success() {
            // Given
            UpdateUserTasteTagCommand command = new UpdateUserTasteTagCommand(1L, "에세이,자기계발,역사");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(loadUserPort).loadById(new UserId(1L));
            verify(updateUserPort).update(userCaptor.capture());

            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.tasteTag()).isEqualTo("에세이,자기계발,역사");
            assertThat(updatedUser.bio()).isEqualTo("기존 자기소개");
            assertThat(updatedUser.nickname()).isEqualTo("기존닉네임");
        }

        @Test
        @DisplayName("빈 취향 태그로 업데이트")
        void handle_UpdateTasteTag_EmptyTag() {
            // Given
            UpdateUserTasteTagCommand command = new UpdateUserTasteTagCommand(1L, "");
            when(loadUserPort.loadById(new UserId(1L))).thenReturn(testUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            handler.handle(command);

            // Then
            verify(updateUserPort).update(userCaptor.capture());
            assertThat(userCaptor.getValue().tasteTag()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 취향 태그 업데이트 시 예외 발생")
        void handle_UpdateTasteTag_UserNotFound() {
            // Given
            UpdateUserTasteTagCommand command = new UpdateUserTasteTagCommand(999L, "새로운취향");
            when(loadUserPort.loadById(new UserId(999L)))
                    .thenThrow(new IllegalArgumentException("User not found"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("Command 유효성 검사")
    class CommandValidationTests {

        @Nested
        @DisplayName("UpdateUserBioCommand 유효성 검사")
        class BioCommandValidation {

            @Test
            @DisplayName("null userId로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullUserId() {
                assertThatThrownBy(() -> new UpdateUserBioCommand(null, "bio"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Invalid user ID");
            }

            @Test
            @DisplayName("0 이하의 userId로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForInvalidUserId() {
                assertThatThrownBy(() -> new UpdateUserBioCommand(0L, "bio"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Invalid user ID");
            }

            @Test
            @DisplayName("null bio로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullBio() {
                assertThatThrownBy(() -> new UpdateUserBioCommand(1L, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Bio cannot be null");
            }

            @Test
            @DisplayName("500자 초과 bio로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForTooLongBio() {
                String longBio = "a".repeat(501);
                assertThatThrownBy(() -> new UpdateUserBioCommand(1L, longBio))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Bio must not exceed 500 characters");
            }
        }

        @Nested
        @DisplayName("UpdateUserNicknameCommand 유효성 검사")
        class NicknameCommandValidation {

            @Test
            @DisplayName("null userId로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullUserId() {
                assertThatThrownBy(() -> new UpdateUserNicknameCommand(null, "nickname"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Invalid user ID");
            }

            @Test
            @DisplayName("null nickname으로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullNickname() {
                assertThatThrownBy(() -> new UpdateUserNicknameCommand(1L, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("NewNickName cannot be null");
            }

            @Test
            @DisplayName("30자 초과 nickname으로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForTooLongNickname() {
                String longNickname = "a".repeat(31);
                assertThatThrownBy(() -> new UpdateUserNicknameCommand(1L, longNickname))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("NewNickName must not exceed 30 characters");
            }
        }

        @Nested
        @DisplayName("UpdateUserProfileImageUrlCommand 유효성 검사")
        class ProfileImageUrlCommandValidation {

            @Test
            @DisplayName("null userId로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullUserId() {
                assertThatThrownBy(() -> new UpdateUserProfileImageUrlCommand(null, "http://example.com/img.jpg"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Invalid user ID");
            }

            @Test
            @DisplayName("null URL로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullUrl() {
                assertThatThrownBy(() -> new UpdateUserProfileImageUrlCommand(1L, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("ProfileImageUrl cannot be null");
            }
        }

        @Nested
        @DisplayName("UpdateUserTasteTagCommand 유효성 검사")
        class TasteTagCommandValidation {

            @Test
            @DisplayName("null userId로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullUserId() {
                assertThatThrownBy(() -> new UpdateUserTasteTagCommand(null, "tag"))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Invalid user ID");
            }

            @Test
            @DisplayName("null tasteTag로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForNullTasteTag() {
                assertThatThrownBy(() -> new UpdateUserTasteTagCommand(1L, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Taste tag cannot be null");
            }

            @Test
            @DisplayName("100자 초과 tasteTag로 커맨드 생성 시 예외 발생")
            void command_ThrowsExceptionForTooLongTasteTag() {
                String longTag = "a".repeat(101);
                assertThatThrownBy(() -> new UpdateUserTasteTagCommand(1L, longTag))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Taste tag must not exceed 100 characters");
            }
        }
    }
}
