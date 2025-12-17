package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MentionParser 도메인 테스트")
class MentionParserTest {

    private static class TestUserFinder implements UserFinder {
        private final Map<String, Long> userMap;

        TestUserFinder(Map<String, Long> userMap) {
            this.userMap = userMap;
        }

        @Override
        public Long findUserIdByUsername(String username) {
            return userMap.get(username);
        }
    }

    @Nested
    @DisplayName("MentionParser parse 메서드")
    class ParseMentions {

        @Test
        @DisplayName("단일 멘션을 파싱할 수 있다")
        void parseSingleMention() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john!";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
            assertThat(mentions.get(0).mentionedUserId()).isEqualTo(1L);
            assertThat(mentions.get(0).startIndex()).isEqualTo(6);
            assertThat(mentions.get(0).endIndex()).isEqualTo(11);
        }

        @Test
        @DisplayName("여러 멘션을 파싱할 수 있다")
        void parseMultipleMentions() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of(
                    "john", 1L,
                    "jane", 2L,
                    "bob", 3L
            ));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john and @jane, what about @bob?";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(3);
            assertThat(mentions.get(0).mentionedUserId()).isEqualTo(1L);
            assertThat(mentions.get(1).mentionedUserId()).isEqualTo(2L);
            assertThat(mentions.get(2).mentionedUserId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 멘션은 무시된다")
        void ignoreNonExistentUserMentions() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john and @unknown!";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
            assertThat(mentions.get(0).mentionedUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("멘션이 없는 텍스트는 빈 리스트를 반환한다")
        void returnEmptyListForTextWithoutMentions() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello world!";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백만 있는 content는 빈 리스트를 반환한다")
        void returnEmptyListForBlankContent(String blankContent) {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);

            // when
            List<Mention> mentions = parser.parse(blankContent);

            // then
            assertThat(mentions).isEmpty();
        }

        @Test
        @DisplayName("같은 사용자를 여러 번 멘션하면 여러 Mention이 생성된다")
        void parseMultipleMentionsOfSameUser() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john! How are you @john?";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(2);
            assertThat(mentions.get(0).mentionedUserId()).isEqualTo(1L);
            assertThat(mentions.get(1).mentionedUserId()).isEqualTo(1L);
            assertThat(mentions.get(0).startIndex()).isNotEqualTo(mentions.get(1).startIndex());
        }
    }

    @Nested
    @DisplayName("MentionParser 멘션 패턴")
    class MentionPattern {

        @Test
        @DisplayName("영문자로 시작하는 사용자명을 파싱할 수 있다")
        void parseAlphabeticUsername() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "@john";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
        }

        @Test
        @DisplayName("숫자를 포함한 사용자명을 파싱할 수 있다")
        void parseUsernameWithNumbers() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("user123", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "@user123";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
        }

        @Test
        @DisplayName("언더스코어를 포함한 사용자명을 파싱할 수 있다")
        void parseUsernameWithUnderscore() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john_doe", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "@john_doe";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
        }

        @Test
        @DisplayName("대소문자가 혼합된 사용자명을 파싱할 수 있다")
        void parseUsernameWithMixedCase() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("JohnDoe", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "@JohnDoe";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
        }

        @Test
        @DisplayName("특수 문자로 구분된 멘션을 파싱할 수 있다")
        void parseMentionWithSpecialCharacterDelimiter() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john, @john! @john?";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(3);
        }

        @Test
        @DisplayName("문장 시작에 있는 멘션을 파싱할 수 있다")
        void parseMentionAtStartOfSentence() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "@john is here";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
            assertThat(mentions.get(0).startIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("문장 끝에 있는 멘션을 파싱할 수 있다")
        void parseMentionAtEndOfSentence() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
            assertThat(mentions.get(0).endIndex()).isEqualTo(content.length());
        }
    }

    @Nested
    @DisplayName("MentionParser 특수 케이스")
    class MentionParserSpecialCases {

        @Test
        @DisplayName("@ 기호만 있고 사용자명이 없으면 파싱되지 않는다")
        void ignoreAtSymbolWithoutUsername() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Email: test@ example.com";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).isEmpty();
        }

        @Test
        @DisplayName("연속된 멘션을 파싱할 수 있다")
        void parseConsecutiveMentions() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of(
                    "john", 1L,
                    "jane", 2L
            ));
            MentionParser parser = new MentionParser(userFinder);
            String content = "@john@jane";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(2);
        }

        @Test
        @DisplayName("멘션 인덱스가 정확하다")
        void verifyMentionIndices() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of("john", 1L));
            MentionParser parser = new MentionParser(userFinder);
            String content = "Hello @john world";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(1);
            Mention mention = mentions.get(0);
            assertThat(content.substring(mention.startIndex(), mention.endIndex()))
                    .isEqualTo("@john");
        }

        @Test
        @DisplayName("긴 텍스트에서 여러 멘션을 파싱할 수 있다")
        void parseMultipleMentionsInLongText() {
            // given
            UserFinder userFinder = new TestUserFinder(Map.of(
                    "alice", 1L,
                    "bob", 2L,
                    "charlie", 3L
            ));
            MentionParser parser = new MentionParser(userFinder);
            String content = "This is a long text with @alice mentioned first, " +
                    "then @bob in the middle, and finally @charlie at the end.";

            // when
            List<Mention> mentions = parser.parse(content);

            // then
            assertThat(mentions).hasSize(3);
            assertThat(mentions.get(0).mentionedUserId()).isEqualTo(1L);
            assertThat(mentions.get(1).mentionedUserId()).isEqualTo(2L);
            assertThat(mentions.get(2).mentionedUserId()).isEqualTo(3L);
        }
    }
}
