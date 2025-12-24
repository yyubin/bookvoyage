package org.yyubin.domain.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReadingStatusTest {

    @Test
    @DisplayName("문자열로 ReadingStatus 생성")
    void from_withValidString() {
        assertThat(ReadingStatus.from("WANT_TO_READ")).isEqualTo(ReadingStatus.WANT_TO_READ);
        assertThat(ReadingStatus.from("READING")).isEqualTo(ReadingStatus.READING);
        assertThat(ReadingStatus.from("COMPLETED")).isEqualTo(ReadingStatus.COMPLETED);
    }

    @Test
    @DisplayName("소문자로도 ReadingStatus 생성 가능")
    void from_withLowerCase() {
        assertThat(ReadingStatus.from("reading")).isEqualTo(ReadingStatus.READING);
    }

    @Test
    @DisplayName("null 또는 빈 문자열이면 기본값 WANT_TO_READ 반환")
    void from_withNullOrBlank_returnsDefault() {
        assertThat(ReadingStatus.from(null)).isEqualTo(ReadingStatus.WANT_TO_READ);
        assertThat(ReadingStatus.from("")).isEqualTo(ReadingStatus.WANT_TO_READ);
        assertThat(ReadingStatus.from("   ")).isEqualTo(ReadingStatus.WANT_TO_READ);
    }

    @Test
    @DisplayName("유효하지 않은 문자열이면 예외 발생")
    void from_withInvalidString_throwsException() {
        assertThatThrownBy(() -> ReadingStatus.from("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid reading status: INVALID");
    }

    @Test
    @DisplayName("상태 조회 메서드")
    void statusCheckMethods() {
        assertThat(ReadingStatus.WANT_TO_READ.isWantToRead()).isTrue();
        assertThat(ReadingStatus.WANT_TO_READ.isReading()).isFalse();
        assertThat(ReadingStatus.WANT_TO_READ.isCompleted()).isFalse();

        assertThat(ReadingStatus.READING.isWantToRead()).isFalse();
        assertThat(ReadingStatus.READING.isReading()).isTrue();
        assertThat(ReadingStatus.READING.isCompleted()).isFalse();

        assertThat(ReadingStatus.COMPLETED.isWantToRead()).isFalse();
        assertThat(ReadingStatus.COMPLETED.isReading()).isFalse();
        assertThat(ReadingStatus.COMPLETED.isCompleted()).isTrue();
    }
}
