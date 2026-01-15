package org.yyubin.support.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CookieProperties 테스트")
class CookiePropertiesTest {

    private CookieProperties cookieProperties;

    @BeforeEach
    void setUp() {
        cookieProperties = new CookieProperties();
    }

    @Test
    @DisplayName("기본값이 올바르게 설정되어 있다")
    void defaultValues() {
        // then
        assertThat(cookieProperties.isSecure()).isTrue();
        assertThat(cookieProperties.getSameSite()).isEqualTo("Lax");
        assertThat(cookieProperties.getDomain()).isNull();
        assertThat(cookieProperties.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("secure 속성을 설정할 수 있다")
    void setSecure() {
        // when
        cookieProperties.setSecure(false);

        // then
        assertThat(cookieProperties.isSecure()).isFalse();
    }

    @Test
    @DisplayName("sameSite 속성을 설정할 수 있다")
    void setSameSite() {
        // when
        cookieProperties.setSameSite("Strict");

        // then
        assertThat(cookieProperties.getSameSite()).isEqualTo("Strict");
    }

    @Test
    @DisplayName("domain 속성을 설정할 수 있다")
    void setDomain() {
        // when
        cookieProperties.setDomain("example.com");

        // then
        assertThat(cookieProperties.getDomain()).isEqualTo("example.com");
    }

    @Test
    @DisplayName("path 속성을 설정할 수 있다")
    void setPath() {
        // when
        cookieProperties.setPath("/api");

        // then
        assertThat(cookieProperties.getPath()).isEqualTo("/api");
    }

    @Test
    @DisplayName("모든 속성을 동시에 설정할 수 있다")
    void setAllProperties() {
        // when
        cookieProperties.setSecure(false);
        cookieProperties.setSameSite("None");
        cookieProperties.setDomain("bookvoyage.com");
        cookieProperties.setPath("/api/v1");

        // then
        assertThat(cookieProperties.isSecure()).isFalse();
        assertThat(cookieProperties.getSameSite()).isEqualTo("None");
        assertThat(cookieProperties.getDomain()).isEqualTo("bookvoyage.com");
        assertThat(cookieProperties.getPath()).isEqualTo("/api/v1");
    }
}
