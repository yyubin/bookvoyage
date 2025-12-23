package org.yyubin.support.nickname;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NicknameGenerator 테스트")
class NicknameGeneratorTest {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-z]+-[a-z]+-\\d{4}$");

    @Test
    @DisplayName("닉네임 생성 성공")
    void generate_Success() {
        // Given
        String seed = "test@example.com";

        // When
        String nickname = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname).isNotNull();
        assertThat(nickname).isNotEmpty();
    }

    @Test
    @DisplayName("생성된 닉네임이 올바른 형식을 따름 (형용사-명사-숫자)")
    void generate_FollowsCorrectFormat() {
        // Given
        String seed = "user@example.com";

        // When
        String nickname = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname).matches(NICKNAME_PATTERN);

        // 형식 검증: adjective-noun-0000
        String[] parts = nickname.split("-");
        assertThat(parts).hasSize(3);
        assertThat(parts[0]).matches("[a-z]+"); // 형용사
        assertThat(parts[1]).matches("[a-z]+"); // 명사
        assertThat(parts[2]).matches("\\d{4}");  // 4자리 숫자
    }

    @Test
    @DisplayName("동일한 seed로 생성한 닉네임은 항상 같음 (결정적)")
    void generate_DeterministicWithSameSeed() {
        // Given
        String seed = "test@example.com";

        // When
        String nickname1 = NicknameGenerator.generate(seed);
        String nickname2 = NicknameGenerator.generate(seed);
        String nickname3 = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname1).isEqualTo(nickname2);
        assertThat(nickname2).isEqualTo(nickname3);
    }

    @Test
    @DisplayName("다른 seed로 생성한 닉네임은 다름")
    void generate_DifferentSeedsProduceDifferentNicknames() {
        // Given
        String seed1 = "user1@example.com";
        String seed2 = "user2@example.com";

        // When
        String nickname1 = NicknameGenerator.generate(seed1);
        String nickname2 = NicknameGenerator.generate(seed2);

        // Then
        assertThat(nickname1).isNotEqualTo(nickname2);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user123@gmail.com",
            "admin@company.org",
            "john.doe@example.com",
            "alice+work@example.com",
            "bob_smith@test.co.uk"
    })
    @DisplayName("다양한 이메일 형식으로 닉네임 생성 성공")
    void generate_WithVariousEmailFormats(String email) {
        // When
        String nickname = NicknameGenerator.generate(email);

        // Then
        assertThat(nickname).matches(NICKNAME_PATTERN);
    }

    @Test
    @DisplayName("빈 문자열로 닉네임 생성")
    void generate_WithEmptyString() {
        // Given
        String seed = "";

        // When
        String nickname = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname).isNotNull();
        assertThat(nickname).matches(NICKNAME_PATTERN);
    }

    @Test
    @DisplayName("매우 긴 seed로 닉네임 생성")
    void generate_WithVeryLongSeed() {
        // Given
        String seed = "a".repeat(1000);

        // When
        String nickname = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname).matches(NICKNAME_PATTERN);
    }

    @Test
    @DisplayName("특수 문자가 포함된 seed로 닉네임 생성")
    void generate_WithSpecialCharacters() {
        // Given
        String seed = "user!@#$%^&*()_+-=[]{}|;:',.<>?/~`";

        // When
        String nickname = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname).matches(NICKNAME_PATTERN);
    }

    @Test
    @DisplayName("유니코드 문자가 포함된 seed로 닉네임 생성")
    void generate_WithUnicodeCharacters() {
        // Given
        String seed = "사용자@예제.한국";

        // When
        String nickname = NicknameGenerator.generate(seed);

        // Then
        assertThat(nickname).matches(NICKNAME_PATTERN);
    }

    @Test
    @DisplayName("숫자 부분이 4자리로 0 패딩됨")
    void generate_NumberPartIsPaddedWithZeros() {
        // Given & When & Then
        // 여러 seed를 시도하여 숫자가 작은 경우를 찾아 0 패딩 확인
        for (int i = 0; i < 100; i++) {
            String seed = "test" + i;
            String nickname = NicknameGenerator.generate(seed);
            String[] parts = nickname.split("-");
            String numberPart = parts[2];

            assertThat(numberPart).hasSize(4);
            assertThat(numberPart).matches("\\d{4}");
        }
    }

    @Test
    @DisplayName("대량의 닉네임 생성 시 충돌률 확인")
    void generate_LowCollisionRate() {
        // Given
        int count = 1000;
        Set<String> nicknames = new HashSet<>();

        // When
        for (int i = 0; i < count; i++) {
            String seed = "user" + i + "@example.com";
            String nickname = NicknameGenerator.generate(seed);
            nicknames.add(nickname);
        }

        // Then
        // 1000개 생성 시 대부분 유니크해야 함 (해시 기반이므로 충돌 가능성은 있음)
        // 최소 95% 이상의 유니크율 기대
        assertThat(nicknames.size()).isGreaterThan((int) (count * 0.95));
    }

    @Test
    @DisplayName("같은 seed로 여러 번 호출해도 성능 일관성")
    void generate_ConsistentPerformance() {
        // Given
        String seed = "performance@test.com";

        // When & Then
        long startTime = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            NicknameGenerator.generate(seed);
        }
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000; // ms로 변환
        System.out.println("10,000 generations took: " + duration + "ms");

        // 10,000번 생성이 1초 이내에 완료되어야 함
        assertThat(duration).isLessThan(1000);
    }

    @Test
    @DisplayName("숫자 범위가 0~9999 사이임")
    void generate_NumberRangeIsValid() {
        // Given & When & Then
        Set<String> numbers = new HashSet<>();

        // 충분히 많은 샘플로 0~9999 범위 확인
        for (int i = 0; i < 10000; i++) {
            String seed = "test" + i + "@example.com";
            String nickname = NicknameGenerator.generate(seed);
            String[] parts = nickname.split("-");
            String numberPart = parts[2];

            int number = Integer.parseInt(numberPart);
            assertThat(number).isBetween(0, 9999);
            numbers.add(numberPart);
        }

        // 다양한 숫자가 생성되었는지 확인
        assertThat(numbers.size()).isGreaterThan(100);
    }

    @Test
    @DisplayName("연속된 seed로 생성한 닉네임은 다름")
    void generate_ConsecutiveSeedsProduceDifferentNicknames() {
        // Given
        String seed1 = "user1";
        String seed2 = "user2";
        String seed3 = "user3";

        // When
        String nickname1 = NicknameGenerator.generate(seed1);
        String nickname2 = NicknameGenerator.generate(seed2);
        String nickname3 = NicknameGenerator.generate(seed3);

        // Then
        assertThat(nickname1).isNotEqualTo(nickname2);
        assertThat(nickname2).isNotEqualTo(nickname3);
        assertThat(nickname1).isNotEqualTo(nickname3);
    }
}
