package org.yyubin.domain.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReadingProgressTest {

    @Test
    @DisplayName("유효한 진행률로 생성")
    void of_withValidPercentage() {
        // when
        ReadingProgress progress = ReadingProgress.of(50);

        // then
        assertThat(progress.getPercentage()).isEqualTo(50);
    }

    @Test
    @DisplayName("0% 진행률")
    void notStarted() {
        // when
        ReadingProgress progress = ReadingProgress.notStarted();

        // then
        assertThat(progress.getPercentage()).isEqualTo(0);
        assertThat(progress.isNotStarted()).isTrue();
        assertThat(progress.isComplete()).isFalse();
    }

    @Test
    @DisplayName("100% 진행률")
    void completed() {
        // when
        ReadingProgress progress = ReadingProgress.completed();

        // then
        assertThat(progress.getPercentage()).isEqualTo(100);
        assertThat(progress.isComplete()).isTrue();
        assertThat(progress.isNotStarted()).isFalse();
    }

    @Test
    @DisplayName("진행률이 0보다 작으면 예외 발생")
    void of_withNegativePercentage_throwsException() {
        assertThatThrownBy(() -> ReadingProgress.of(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Progress must be between 0 and 100");
    }

    @Test
    @DisplayName("진행률이 100보다 크면 예외 발생")
    void of_withPercentageOver100_throwsException() {
        assertThatThrownBy(() -> ReadingProgress.of(101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Progress must be between 0 and 100");
    }
}
