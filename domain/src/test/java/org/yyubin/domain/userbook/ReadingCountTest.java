package org.yyubin.domain.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReadingCountTest {

    @Test
    @DisplayName("유효한 읽기 횟수로 생성")
    void of_withValidCount() {
        // when
        ReadingCount count = ReadingCount.of(3);

        // then
        assertThat(count.getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("첫 번째 읽기 생성")
    void first() {
        // when
        ReadingCount count = ReadingCount.first();

        // then
        assertThat(count.getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("읽기 횟수 증가")
    void increment() {
        // given
        ReadingCount count = ReadingCount.of(2);

        // when
        ReadingCount incremented = count.increment();

        // then
        assertThat(incremented.getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("읽기 횟수가 1보다 작으면 예외 발생")
    void of_withCountLessThan1_throwsException() {
        assertThatThrownBy(() -> ReadingCount.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reading count must be at least 1");
    }
}
