package org.yyubin.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WebNovelPlatform enum tests")
class WebNovelPlatformTest {

    @Test
    @DisplayName("platform metadata is exposed")
    void platformMetadata() {
        WebNovelPlatform platform = WebNovelPlatform.KAKAO_PAGE;

        assertThat(platform.getDisplayName()).isNotBlank();
        assertThat(platform.getBaseUrl()).contains("kakao.com");
    }

    @Test
    @DisplayName("other platform can have null url")
    void otherHasNullUrl() {
        assertThat(WebNovelPlatform.OTHER.getBaseUrl()).isNull();
    }
}
