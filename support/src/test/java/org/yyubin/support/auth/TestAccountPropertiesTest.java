package org.yyubin.support.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestAccountPropertiesTest {

    private TestAccountProperties testAccountProperties;

    @BeforeEach
    void setUp() {
        testAccountProperties = new TestAccountProperties();
        testAccountProperties.setEmailDomain("test.bookvoyage.com");
    }

    @Test
    @DisplayName("테스트 도메인 이메일은 true를 반환한다")
    void shouldReturnTrueForTestDomainEmail() {
        // given
        String testEmail = "user1@test.bookvoyage.com";

        // when
        boolean result = testAccountProperties.isTestEmail(testEmail);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("대소문자 관계없이 테스트 도메인 이메일을 인식한다")
    void shouldBeCaseInsensitive() {
        // given
        String upperCaseEmail = "USER@TEST.BOOKVOYAGE.COM";
        String mixedCaseEmail = "User@Test.BookVoyage.Com";

        // when & then
        assertThat(testAccountProperties.isTestEmail(upperCaseEmail)).isTrue();
        assertThat(testAccountProperties.isTestEmail(mixedCaseEmail)).isTrue();
    }

    @Test
    @DisplayName("다른 도메인 이메일은 false를 반환한다")
    void shouldReturnFalseForOtherDomainEmail() {
        // given
        String gmailEmail = "user@gmail.com";
        String naverEmail = "user@naver.com";

        // when & then
        assertThat(testAccountProperties.isTestEmail(gmailEmail)).isFalse();
        assertThat(testAccountProperties.isTestEmail(naverEmail)).isFalse();
    }

    @Test
    @DisplayName("null 이메일은 false를 반환한다")
    void shouldReturnFalseForNullEmail() {
        // when
        boolean result = testAccountProperties.isTestEmail(null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 이메일은 false를 반환한다")
    void shouldReturnFalseForEmptyEmail() {
        // when & then
        assertThat(testAccountProperties.isTestEmail("")).isFalse();
        assertThat(testAccountProperties.isTestEmail("   ")).isFalse();
    }

    @Test
    @DisplayName("도메인 설정이 없으면 false를 반환한다")
    void shouldReturnFalseWhenDomainNotConfigured() {
        // given
        testAccountProperties.setEmailDomain(null);

        // when
        boolean result = testAccountProperties.isTestEmail("user@test.bookvoyage.com");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("도메인이 빈 문자열이면 false를 반환한다")
    void shouldReturnFalseWhenDomainIsEmpty() {
        // given
        testAccountProperties.setEmailDomain("");

        // when
        boolean result = testAccountProperties.isTestEmail("user@test.bookvoyage.com");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("도메인이 공백만 있으면 false를 반환한다")
    void shouldReturnFalseWhenDomainIsBlank() {
        // given
        testAccountProperties.setEmailDomain("   ");

        // when
        boolean result = testAccountProperties.isTestEmail("user@test.bookvoyage.com");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getter와 setter가 정상 동작한다")
    void shouldGetAndSetEmailDomain() {
        // when
        testAccountProperties.setEmailDomain("custom.domain.com");

        // then
        assertThat(testAccountProperties.getEmailDomain()).isEqualTo("custom.domain.com");
    }
}
