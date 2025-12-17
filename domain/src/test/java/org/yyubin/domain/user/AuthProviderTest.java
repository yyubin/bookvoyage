package org.yyubin.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuthProvider 도메인 테스트")
class AuthProviderTest {

    @Nested
    @DisplayName("AuthProvider Enum 값")
    class AuthProviderValues {

        @Test
        @DisplayName("LOCAL 값이 존재한다")
        void localValueExists() {
            // when
            AuthProvider provider = AuthProvider.LOCAL;

            // then
            assertThat(provider).isNotNull();
            assertThat(provider.name()).isEqualTo("LOCAL");
        }

        @Test
        @DisplayName("GOOGLE 값이 존재한다")
        void googleValueExists() {
            // when
            AuthProvider provider = AuthProvider.GOOGLE;

            // then
            assertThat(provider).isNotNull();
            assertThat(provider.name()).isEqualTo("GOOGLE");
        }

        @Test
        @DisplayName("KAKAO 값이 존재한다")
        void kakaoValueExists() {
            // when
            AuthProvider provider = AuthProvider.KAKAO;

            // then
            assertThat(provider).isNotNull();
            assertThat(provider.name()).isEqualTo("KAKAO");
        }

        @Test
        @DisplayName("NAVER 값이 존재한다")
        void naverValueExists() {
            // when
            AuthProvider provider = AuthProvider.NAVER;

            // then
            assertThat(provider).isNotNull();
            assertThat(provider.name()).isEqualTo("NAVER");
        }

        @Test
        @DisplayName("모든 AuthProvider 값은 4개이다")
        void allValuesCount() {
            // when
            AuthProvider[] values = AuthProvider.values();

            // then
            assertThat(values).hasSize(4);
            assertThat(values).containsExactly(
                    AuthProvider.LOCAL,
                    AuthProvider.GOOGLE,
                    AuthProvider.KAKAO,
                    AuthProvider.NAVER
            );
        }
    }

    @Nested
    @DisplayName("AuthProvider valueOf 메서드")
    class AuthProviderValueOf {

        @Test
        @DisplayName("'LOCAL' 문자열로 LOCAL을 가져올 수 있다")
        void valueOfLocal() {
            // when
            AuthProvider provider = AuthProvider.valueOf("LOCAL");

            // then
            assertThat(provider).isEqualTo(AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("'GOOGLE' 문자열로 GOOGLE을 가져올 수 있다")
        void valueOfGoogle() {
            // when
            AuthProvider provider = AuthProvider.valueOf("GOOGLE");

            // then
            assertThat(provider).isEqualTo(AuthProvider.GOOGLE);
        }

        @Test
        @DisplayName("'KAKAO' 문자열로 KAKAO를 가져올 수 있다")
        void valueOfKakao() {
            // when
            AuthProvider provider = AuthProvider.valueOf("KAKAO");

            // then
            assertThat(provider).isEqualTo(AuthProvider.KAKAO);
        }

        @Test
        @DisplayName("'NAVER' 문자열로 NAVER를 가져올 수 있다")
        void valueOfNaver() {
            // when
            AuthProvider provider = AuthProvider.valueOf("NAVER");

            // then
            assertThat(provider).isEqualTo(AuthProvider.NAVER);
        }

        @Test
        @DisplayName("유효하지 않은 값으로 valueOf 호출 시 예외가 발생한다")
        void valueOfInvalidValue() {
            // when & then
            assertThatThrownBy(() -> AuthProvider.valueOf("FACEBOOK"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("소문자로 valueOf 호출 시 예외가 발생한다")
        void valueOfLowercase() {
            // when & then
            assertThatThrownBy(() -> AuthProvider.valueOf("local"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("AuthProvider 동등성")
    class AuthProviderEquality {

        @Test
        @DisplayName("같은 AuthProvider 값은 동등하다")
        void sameProvidersAreEqual() {
            // given
            AuthProvider provider1 = AuthProvider.LOCAL;
            AuthProvider provider2 = AuthProvider.LOCAL;

            // when & then
            assertThat(provider1).isEqualTo(provider2);
            assertThat(provider1).isSameAs(provider2);
        }

        @Test
        @DisplayName("다른 AuthProvider 값은 동등하지 않다")
        void differentProvidersAreNotEqual() {
            // given
            AuthProvider provider1 = AuthProvider.LOCAL;
            AuthProvider provider2 = AuthProvider.GOOGLE;

            // when & then
            assertThat(provider1).isNotEqualTo(provider2);
        }
    }

    @Nested
    @DisplayName("AuthProvider toString")
    class AuthProviderToString {

        @Test
        @DisplayName("LOCAL의 toString()은 'LOCAL'을 반환한다")
        void localToString() {
            // given
            AuthProvider provider = AuthProvider.LOCAL;

            // when
            String result = provider.toString();

            // then
            assertThat(result).isEqualTo("LOCAL");
        }

        @Test
        @DisplayName("GOOGLE의 toString()은 'GOOGLE'을 반환한다")
        void googleToString() {
            // given
            AuthProvider provider = AuthProvider.GOOGLE;

            // when
            String result = provider.toString();

            // then
            assertThat(result).isEqualTo("GOOGLE");
        }

        @Test
        @DisplayName("KAKAO의 toString()은 'KAKAO'를 반환한다")
        void kakaoToString() {
            // given
            AuthProvider provider = AuthProvider.KAKAO;

            // when
            String result = provider.toString();

            // then
            assertThat(result).isEqualTo("KAKAO");
        }

        @Test
        @DisplayName("NAVER의 toString()은 'NAVER'를 반환한다")
        void naverToString() {
            // given
            AuthProvider provider = AuthProvider.NAVER;

            // when
            String result = provider.toString();

            // then
            assertThat(result).isEqualTo("NAVER");
        }
    }
}
