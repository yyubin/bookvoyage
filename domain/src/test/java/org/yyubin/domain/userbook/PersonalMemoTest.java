package org.yyubin.domain.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PersonalMemoTest {

    @Test
    @DisplayName("유효한 메모로 생성")
    void of_withValidMemo() {
        // given
        String content = "재미있는 책이었다";

        // when
        PersonalMemo memo = PersonalMemo.of(content);

        // then
        assertThat(memo.getContent()).isEqualTo(content);
        assertThat(memo.hasContent()).isTrue();
    }

    @Test
    @DisplayName("null 메모로 생성")
    void of_withNullMemo() {
        // when
        PersonalMemo memo = PersonalMemo.of(null);

        // then
        assertThat(memo.getContent()).isNull();
        assertThat(memo.hasContent()).isFalse();
    }

    @Test
    @DisplayName("빈 메모 생성")
    void empty() {
        // when
        PersonalMemo memo = PersonalMemo.empty();

        // then
        assertThat(memo.getContent()).isNull();
        assertThat(memo.hasContent()).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 메모는 내용이 없는 것으로 간주")
    void of_withBlankString_hasNoContent() {
        // when
        PersonalMemo memo = PersonalMemo.of("   ");

        // then
        assertThat(memo.hasContent()).isFalse();
    }

    @Test
    @DisplayName("메모가 2000자를 초과하면 예외 발생")
    void of_withMemoExceeding2000Characters_throwsException() {
        // given
        String longMemo = "a".repeat(2001);

        // when & then
        assertThatThrownBy(() -> PersonalMemo.of(longMemo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Memo cannot exceed 2000 characters");
    }

    @Test
    @DisplayName("메모가 정확히 2000자일 때는 허용")
    void of_withExactly2000Characters_succeeds() {
        // given
        String memo2000 = "a".repeat(2000);

        // when
        PersonalMemo memo = PersonalMemo.of(memo2000);

        // then
        assertThat(memo.getContent()).hasSize(2000);
    }
}
