package org.yyubin.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Nested
    @DisplayName("User 생성 - LOCAL provider")
    class CreateUserWithLocalProvider {

        @Test
        @DisplayName("유효한 데이터로 LOCAL User를 생성할 수 있다")
        void createLocalUserWithValidData() {
            // given
            UserId id = new UserId(1L);
            String email = "user@example.com";
            String username = "testuser";
            String password = "password123";
            String nickname = "testnick";
            String bio = "Test bio";
            String tasteTag = "fiction";
            Role role = Role.USER;
            AuthProvider provider = AuthProvider.LOCAL;
            String profileImageUrl = "https://example.com/image.jpg";
            LocalDateTime createdAt = LocalDateTime.now();

            // when
            User user = new User(id, email, username, password, nickname, bio, tasteTag, role, provider, profileImageUrl, createdAt);

            // then
            assertThat(user).isNotNull();
            assertThat(user.id()).isEqualTo(id);
            assertThat(user.email()).isEqualTo(email);
            assertThat(user.username()).isEqualTo(username);
            assertThat(user.password()).isEqualTo(password);
            assertThat(user.nickname()).isEqualTo(nickname);
            assertThat(user.bio()).isEqualTo(bio);
            assertThat(user.tasteTag()).isEqualTo(tasteTag);
            assertThat(user.role()).isEqualTo(role);
            assertThat(user.provider()).isEqualTo(provider);
            assertThat(user.ProfileImageUrl()).isEqualTo(profileImageUrl);
            assertThat(user.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("LOCAL provider는 password가 필수이다")
        void localProviderRequiresPassword() {
            // when & then
            assertThatThrownBy(() -> new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    null,
                    null,
                    null,
                    null,
                    null,
                    AuthProvider.LOCAL,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password cannot be empty for LOCAL provider");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("LOCAL provider에서 빈 password로 생성 시 예외가 발생한다")
        void localProviderWithBlankPassword(String blankPassword) {
            // when & then
            assertThatThrownBy(() -> new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    blankPassword,
                    null,
                    null,
                    null,
                    null,
                    AuthProvider.LOCAL,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password cannot be empty for LOCAL provider");
        }
    }

    @Nested
    @DisplayName("User 생성 - OAuth provider")
    class CreateUserWithOAuthProvider {

        @Test
        @DisplayName("유효한 데이터로 GOOGLE User를 생성할 수 있다")
        void createGoogleUserWithValidData() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@gmail.com",
                    "testuser",
                    null,
                    "testnick",
                    "Test bio",
                    null,
                    Role.USER,
                    AuthProvider.GOOGLE,
                    "https://example.com/image.jpg",
                    LocalDateTime.now()
            );

            // then
            assertThat(user).isNotNull();
            assertThat(user.provider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(user.password()).isEmpty();
        }

        @Test
        @DisplayName("KAKAO provider는 password 없이 생성할 수 있다")
        void createKakaoUserWithoutPassword() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@kakao.com",
                    "testuser",
                    null,
                    null,
                    null,
                    null,
                    null,
                    AuthProvider.KAKAO,
                    null,
                    null
            );

            // then
            assertThat(user).isNotNull();
            assertThat(user.provider()).isEqualTo(AuthProvider.KAKAO);
            assertThat(user.password()).isEmpty();
        }

        @Test
        @DisplayName("NAVER provider는 password 없이 생성할 수 있다")
        void createNaverUserWithoutPassword() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@naver.com",
                    "testuser",
                    null,
                    null,
                    null,
                    null,
                    null,
                    AuthProvider.NAVER,
                    null,
                    null
            );

            // then
            assertThat(user).isNotNull();
            assertThat(user.provider()).isEqualTo(AuthProvider.NAVER);
            assertThat(user.password()).isEmpty();
        }
    }

    @Nested
    @DisplayName("User 기본값 설정")
    class UserDefaultValues {

        @Test
        @DisplayName("null provider는 LOCAL로 기본 설정된다")
        void nullProviderDefaultsToLocal() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            assertThat(user.provider()).isEqualTo(AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("null bio는 빈 문자열로 기본 설정된다")
        void nullBioDefaultsToEmptyString() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            assertThat(user.bio()).isEmpty();
        }

        @Test
        @DisplayName("null tasteTag는 빈 문자열로 기본 설정된다")
        void nullTasteTagDefaultsToEmptyString() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            assertThat(user.tasteTag()).isEmpty();
        }

        @Test
        @DisplayName("null role은 USER로 기본 설정된다")
        void nullRoleDefaultsToUser() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            assertThat(user.role()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("null createdAt은 현재 시간으로 기본 설정된다")
        void nullCreatedAtDefaultsToNow() {
            // given
            LocalDateTime before = LocalDateTime.now();

            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(user.createdAt()).isAfterOrEqualTo(before);
            assertThat(user.createdAt()).isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("User 유효성 검증 - email")
    class ValidateEmail {

        @Test
        @DisplayName("유효한 이메일로 User를 생성할 수 있다")
        void createWithValidEmail() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            assertThat(user.email()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("null email로 생성 시 예외가 발생한다")
        void createWithNullEmail() {
            // when & then
            assertThatThrownBy(() -> new User(
                    new UserId(1L),
                    null,
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email");
        }

        @Test
        @DisplayName("@ 기호가 없는 email로 생성 시 예외가 발생한다")
        void createWithEmailWithoutAtSymbol() {
            // when & then
            assertThatThrownBy(() -> new User(
                    new UserId(1L),
                    "userexample.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email");
        }
    }

    @Nested
    @DisplayName("User 유효성 검증 - username")
    class ValidateUsername {

        @Test
        @DisplayName("유효한 username으로 User를 생성할 수 있다")
        void createWithValidUsername() {
            // when
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // then
            assertThat(user.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("null username으로 생성 시 예외가 발생한다")
        void createWithNullUsername() {
            // when & then
            assertThatThrownBy(() -> new User(
                    new UserId(1L),
                    "user@example.com",
                    null,
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username cannot be empty");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("빈 username으로 생성 시 예외가 발생한다")
        void createWithBlankUsername(String blankUsername) {
            // when & then
            assertThatThrownBy(() -> new User(
                    new UserId(1L),
                    "user@example.com",
                    blankUsername,
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username cannot be empty");
        }
    }

    @Nested
    @DisplayName("User updateProfile 메서드")
    class UpdateUserProfile {

        @Test
        @DisplayName("updateProfile로 username을 업데이트할 수 있다")
        void updateUsername() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "oldusername",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            String newUsername = "newusername";

            // when
            User updated = user.updateProfile(newUsername, null, null, null, null);

            // then
            assertThat(updated.username()).isEqualTo(newUsername);
            assertThat(user.username()).isEqualTo("oldusername");
        }

        @Test
        @DisplayName("updateProfile로 bio를 업데이트할 수 있다")
        void updateBio() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    "old bio",
                    null,
                    null,
                    null,
                    null,
                    null
            );
            String newBio = "new bio";

            // when
            User updated = user.updateProfile(null, newBio, null, null, null);

            // then
            assertThat(updated.bio()).isEqualTo(newBio);
            assertThat(user.bio()).isEqualTo("old bio");
        }

        @Test
        @DisplayName("updateProfile로 nickname을 업데이트할 수 있다")
        void updateNickname() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    "oldnick",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            String newNickname = "newnick";

            // when
            User updated = user.updateProfile(null, null, newNickname, null, null);

            // then
            assertThat(updated.nickname()).isEqualTo(newNickname);
            assertThat(user.nickname()).isEqualTo("oldnick");
        }

        @Test
        @DisplayName("updateProfile로 profileImageUrl을 업데이트할 수 있다")
        void updateProfileImageUrl() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    "https://old.com/image.jpg",
                    null
            );
            String newProfileImageUrl = "https://new.com/image.jpg";

            // when
            User updated = user.updateProfile(null, null, null, newProfileImageUrl, null);

            // then
            assertThat(updated.ProfileImageUrl()).isEqualTo(newProfileImageUrl);
            assertThat(user.ProfileImageUrl()).isEqualTo("https://old.com/image.jpg");
        }

        @Test
        @DisplayName("updateProfile로 tasteTag를 업데이트할 수 있다")
        void updateTasteTag() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    "old taste",
                    null,
                    null,
                    null,
                    null
            );
            String newTasteTag = "new taste";

            // when
            User updated = user.updateProfile(null, null, null, null, newTasteTag);

            // then
            assertThat(updated.tasteTag()).isEqualTo(newTasteTag);
            assertThat(user.tasteTag()).isEqualTo("old taste");
        }

        @Test
        @DisplayName("updateProfile로 여러 필드를 동시에 업데이트할 수 있다")
        void updateMultipleFields() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "oldusername",
                    "password123",
                    "oldnick",
                    "old bio",
                    "old taste",
                    null,
                    null,
                    "https://old.com/image.jpg",
                    null
            );

            // when
            User updated = user.updateProfile(
                    "newusername",
                    "new bio",
                    "newnick",
                    "https://new.com/image.jpg",
                    "new taste"
            );

            // then
            assertThat(updated.username()).isEqualTo("newusername");
            assertThat(updated.bio()).isEqualTo("new bio");
            assertThat(updated.nickname()).isEqualTo("newnick");
            assertThat(updated.ProfileImageUrl()).isEqualTo("https://new.com/image.jpg");
            assertThat(updated.tasteTag()).isEqualTo("new taste");
        }

        @Test
        @DisplayName("updateProfile에서 null 값을 전달하면 기존 값이 유지된다")
        void updateProfileWithNullKeepsOriginalValues() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    "testnick",
                    "test bio",
                    "test taste",
                    null,
                    null,
                    "https://example.com/image.jpg",
                    null
            );

            // when
            User updated = user.updateProfile(null, null, null, null, null);

            // then
            assertThat(updated.username()).isEqualTo(user.username());
            assertThat(updated.bio()).isEqualTo(user.bio());
            assertThat(updated.nickname()).isEqualTo(user.nickname());
            assertThat(updated.ProfileImageUrl()).isEqualTo(user.ProfileImageUrl());
            assertThat(updated.tasteTag()).isEqualTo(user.tasteTag());
        }

        @Test
        @DisplayName("updateProfile에서 빈 username은 기존 값이 유지된다")
        void updateProfileWithBlankUsernameKeepsOriginal() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when
            User updated = user.updateProfile("  ", null, null, null, null);

            // then
            assertThat(updated.username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("updateProfile은 불변성을 유지한다")
        void updateProfileIsImmutable() {
            // given
            User original = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            String originalUsername = original.username();

            // when
            User updated = original.updateProfile("newusername", null, null, null, null);

            // then
            assertThat(original.username()).isEqualTo(originalUsername);
            assertThat(updated.username()).isNotEqualTo(originalUsername);
        }
    }

    @Nested
    @DisplayName("User 동등성")
    class UserEquality {

        @Test
        @DisplayName("같은 필드를 가진 User는 동등하다")
        void equalUsersWithSameFields() {
            // given
            UserId id = new UserId(1L);
            String email = "user@example.com";
            String username = "testuser";
            String password = "password123";
            LocalDateTime createdAt = LocalDateTime.now();
            User user1 = new User(id, email, username, password, null, null, null, null, null, null, createdAt);
            User user2 = new User(id, email, username, password, null, null, null, null, null, null, createdAt);

            // when & then
            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 User는 동등하지 않다")
        void notEqualUsersWithDifferentFields() {
            // given
            User user1 = new User(
                    new UserId(1L),
                    "user1@example.com",
                    "testuser1",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            User user2 = new User(
                    new UserId(2L),
                    "user2@example.com",
                    "testuser2",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            assertThat(user1).isNotEqualTo(user2);
        }

        @Test
        @DisplayName("User는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            assertThat(user).isEqualTo(user);
        }

        @Test
        @DisplayName("User는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            User user = new User(
                    new UserId(1L),
                    "user@example.com",
                    "testuser",
                    "password123",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            assertThat(user).isNotEqualTo(null);
        }
    }
}
