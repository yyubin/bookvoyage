package org.yyubin.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Role 도메인 테스트")
class RoleTest {

    @Nested
    @DisplayName("Role Enum 값")
    class RoleValues {

        @Test
        @DisplayName("USER 값이 존재한다")
        void userValueExists() {
            // when
            Role role = Role.USER;

            // then
            assertThat(role).isNotNull();
            assertThat(role.name()).isEqualTo("USER");
        }

        @Test
        @DisplayName("ADMIN 값이 존재한다")
        void adminValueExists() {
            // when
            Role role = Role.ADMIN;

            // then
            assertThat(role).isNotNull();
            assertThat(role.name()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("모든 Role 값은 2개이다")
        void allValuesCount() {
            // when
            Role[] values = Role.values();

            // then
            assertThat(values).hasSize(2);
            assertThat(values).containsExactly(Role.USER, Role.ADMIN);
        }
    }

    @Nested
    @DisplayName("Role valueOf 메서드")
    class RoleValueOf {

        @Test
        @DisplayName("'USER' 문자열로 USER를 가져올 수 있다")
        void valueOfUser() {
            // when
            Role role = Role.valueOf("USER");

            // then
            assertThat(role).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("'ADMIN' 문자열로 ADMIN을 가져올 수 있다")
        void valueOfAdmin() {
            // when
            Role role = Role.valueOf("ADMIN");

            // then
            assertThat(role).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("유효하지 않은 값으로 valueOf 호출 시 예외가 발생한다")
        void valueOfInvalidValue() {
            // when & then
            assertThatThrownBy(() -> Role.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("소문자로 valueOf 호출 시 예외가 발생한다")
        void valueOfLowercase() {
            // when & then
            assertThatThrownBy(() -> Role.valueOf("user"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Role 동등성")
    class RoleEquality {

        @Test
        @DisplayName("같은 Role 값은 동등하다")
        void sameRolesAreEqual() {
            // given
            Role role1 = Role.USER;
            Role role2 = Role.USER;

            // when & then
            assertThat(role1).isEqualTo(role2);
            assertThat(role1).isSameAs(role2);
        }

        @Test
        @DisplayName("다른 Role 값은 동등하지 않다")
        void differentRolesAreNotEqual() {
            // given
            Role role1 = Role.USER;
            Role role2 = Role.ADMIN;

            // when & then
            assertThat(role1).isNotEqualTo(role2);
        }
    }

    @Nested
    @DisplayName("Role toString")
    class RoleToString {

        @Test
        @DisplayName("USER의 toString()은 'USER'를 반환한다")
        void userToString() {
            // given
            Role role = Role.USER;

            // when
            String result = role.toString();

            // then
            assertThat(result).isEqualTo("USER");
        }

        @Test
        @DisplayName("ADMIN의 toString()은 'ADMIN'을 반환한다")
        void adminToString() {
            // given
            Role role = Role.ADMIN;

            // when
            String result = role.toString();

            // then
            assertThat(result).isEqualTo("ADMIN");
        }
    }
}
