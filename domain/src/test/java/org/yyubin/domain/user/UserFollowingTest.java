package org.yyubin.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserFollowing 도메인 테스트")
class UserFollowingTest {

    @Nested
    @DisplayName("UserFollowing 생성 - of 메서드")
    class CreateUserFollowingWithOf {

        @Test
        @DisplayName("유효한 데이터로 UserFollowing을 생성할 수 있다")
        void createWithValidData() {
            // given
            Long id = 1L;
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);
            LocalDateTime createdAt = LocalDateTime.now();

            // when
            UserFollowing userFollowing = UserFollowing.of(id, followerId, followeeId, createdAt);

            // then
            assertThat(userFollowing).isNotNull();
            assertThat(userFollowing.getId()).isEqualTo(id);
            assertThat(userFollowing.getFollowerId()).isEqualTo(followerId);
            assertThat(userFollowing.getFolloweeId()).isEqualTo(followeeId);
            assertThat(userFollowing.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("null ID로 UserFollowing을 생성할 수 있다")
        void createWithNullId() {
            // given
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);
            LocalDateTime createdAt = LocalDateTime.now();

            // when
            UserFollowing userFollowing = UserFollowing.of(null, followerId, followeeId, createdAt);

            // then
            assertThat(userFollowing).isNotNull();
            assertThat(userFollowing.getId()).isNull();
        }

        @Test
        @DisplayName("같은 사용자 ID로 팔로우 생성 시 예외가 발생한다")
        void createWithSameUserIds() {
            // given
            UserId userId = new UserId(1L);
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> UserFollowing.of(1L, userId, userId, createdAt))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot follow themselves");
        }

        @Test
        @DisplayName("null followerId로 생성 시 예외가 발생한다")
        void createWithNullFollowerId() {
            // given
            UserId followeeId = new UserId(2L);
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> UserFollowing.of(1L, null, followeeId, createdAt))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null followeeId로 생성 시 예외가 발생한다")
        void createWithNullFolloweeId() {
            // given
            UserId followerId = new UserId(1L);
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> UserFollowing.of(1L, followerId, null, createdAt))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Followee ID cannot be null");
        }

        @Test
        @DisplayName("null createdAt로 생성 시 예외가 발생한다")
        void createWithNullCreatedAt() {
            // given
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);

            // when & then
            assertThatThrownBy(() -> UserFollowing.of(1L, followerId, followeeId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Created at cannot be null");
        }
    }

    @Nested
    @DisplayName("UserFollowing 생성 - create 메서드")
    class CreateUserFollowingWithCreate {

        @Test
        @DisplayName("create 메서드로 UserFollowing을 생성할 수 있다")
        void createWithValidData() {
            // given
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);

            // when
            UserFollowing userFollowing = UserFollowing.create(followerId, followeeId);

            // then
            assertThat(userFollowing).isNotNull();
            assertThat(userFollowing.getId()).isNull();
            assertThat(userFollowing.getFollowerId()).isEqualTo(followerId);
            assertThat(userFollowing.getFolloweeId()).isEqualTo(followeeId);
            assertThat(userFollowing.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("create 메서드는 현재 시간을 createdAt으로 설정한다")
        void createSetsCurrentTime() {
            // given
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);
            LocalDateTime before = LocalDateTime.now();

            // when
            UserFollowing userFollowing = UserFollowing.create(followerId, followeeId);

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(userFollowing.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(userFollowing.getCreatedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("create 메서드에서도 같은 사용자 ID로 생성 시 예외가 발생한다")
        void createWithSameUserIds() {
            // given
            UserId userId = new UserId(1L);

            // when & then
            assertThatThrownBy(() -> UserFollowing.create(userId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot follow themselves");
        }
    }

    @Nested
    @DisplayName("UserFollowing 동등성")
    class UserFollowingEquality {

        @Test
        @DisplayName("같은 ID를 가진 UserFollowing은 동등하다")
        void equalFollowingsWithSameId() {
            // given
            Long id = 1L;
            UserFollowing following1 = UserFollowing.of(
                    id,
                    new UserId(1L),
                    new UserId(2L),
                    LocalDateTime.now()
            );
            UserFollowing following2 = UserFollowing.of(
                    id,
                    new UserId(3L),
                    new UserId(4L),
                    LocalDateTime.now()
            );

            // when & then
            assertThat(following1).isEqualTo(following2);
            assertThat(following1.hashCode()).isEqualTo(following2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 UserFollowing은 동등하지 않다")
        void notEqualFollowingsWithDifferentId() {
            // given
            UserFollowing following1 = UserFollowing.of(
                    1L,
                    new UserId(1L),
                    new UserId(2L),
                    LocalDateTime.now()
            );
            UserFollowing following2 = UserFollowing.of(
                    2L,
                    new UserId(1L),
                    new UserId(2L),
                    LocalDateTime.now()
            );

            // when & then
            assertThat(following1).isNotEqualTo(following2);
        }

        @Test
        @DisplayName("UserFollowing은 자기 자신과 동등하다")
        void equalToItself() {
            // given
            UserFollowing following = UserFollowing.create(
                    new UserId(1L),
                    new UserId(2L)
            );

            // when & then
            assertThat(following).isEqualTo(following);
        }

        @Test
        @DisplayName("UserFollowing은 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            UserFollowing following = UserFollowing.create(
                    new UserId(1L),
                    new UserId(2L)
            );

            // when & then
            assertThat(following).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("UserFollowing toString")
    class UserFollowingToString {

        @Test
        @DisplayName("toString()은 UserFollowing 정보를 포함한 문자열을 반환한다")
        void toStringContainsFollowingInfo() {
            // given
            UserFollowing following = UserFollowing.create(
                    new UserId(1L),
                    new UserId(2L)
            );

            // when
            String result = following.toString();

            // then
            assertThat(result).contains("UserFollowing");
            assertThat(result).contains("followerId=");
            assertThat(result).contains("followeeId=");
        }
    }

    @Nested
    @DisplayName("UserFollowing 필드 접근")
    class UserFollowingFieldAccess {

        @Test
        @DisplayName("모든 getter 메서드로 필드에 접근할 수 있다")
        void accessAllFields() {
            // given
            Long id = 1L;
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);
            LocalDateTime createdAt = LocalDateTime.now();
            UserFollowing following = UserFollowing.of(id, followerId, followeeId, createdAt);

            // when & then
            assertThat(following.getId()).isEqualTo(id);
            assertThat(following.getFollowerId()).isEqualTo(followerId);
            assertThat(following.getFolloweeId()).isEqualTo(followeeId);
            assertThat(following.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("UserFollowing 비즈니스 규칙")
    class UserFollowingBusinessRules {

        @Test
        @DisplayName("사용자는 자기 자신을 팔로우할 수 없다")
        void cannotFollowSelf() {
            // given
            UserId userId = new UserId(1L);

            // when & then
            assertThatThrownBy(() -> UserFollowing.create(userId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot follow themselves");
        }

        @Test
        @DisplayName("다른 사용자를 팔로우할 수 있다")
        void canFollowOtherUser() {
            // given
            UserId followerId = new UserId(1L);
            UserId followeeId = new UserId(2L);

            // when
            UserFollowing following = UserFollowing.create(followerId, followeeId);

            // then
            assertThat(following).isNotNull();
            assertThat(following.getFollowerId()).isEqualTo(followerId);
            assertThat(following.getFolloweeId()).isEqualTo(followeeId);
        }

        @Test
        @DisplayName("양방향 팔로우가 가능하다")
        void bidirectionalFollowingAllowed() {
            // given
            UserId user1 = new UserId(1L);
            UserId user2 = new UserId(2L);

            // when
            UserFollowing following1 = UserFollowing.create(user1, user2);
            UserFollowing following2 = UserFollowing.create(user2, user1);

            // then
            assertThat(following1).isNotNull();
            assertThat(following2).isNotNull();
            assertThat(following1.getFollowerId()).isEqualTo(user1);
            assertThat(following1.getFolloweeId()).isEqualTo(user2);
            assertThat(following2.getFollowerId()).isEqualTo(user2);
            assertThat(following2.getFolloweeId()).isEqualTo(user1);
        }
    }
}
