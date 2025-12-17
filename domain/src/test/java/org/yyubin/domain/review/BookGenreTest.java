package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookGenre 도메인 테스트")
class BookGenreTest {

    @Nested
    @DisplayName("BookGenre Enum 값")
    class BookGenreValues {

        @Test
        @DisplayName("모든 문학 장르가 존재한다")
        void literatureGenresExist() {
            // when & then
            assertThat(BookGenre.FICTION).isNotNull();
            assertThat(BookGenre.CLASSIC).isNotNull();
            assertThat(BookGenre.POETRY).isNotNull();
            assertThat(BookGenre.ESSAY).isNotNull();
        }

        @Test
        @DisplayName("모든 장르 문학 장르가 존재한다")
        void genreLiteratureGenresExist() {
            // when & then
            assertThat(BookGenre.FANTASY).isNotNull();
            assertThat(BookGenre.SCIENCE_FICTION).isNotNull();
            assertThat(BookGenre.MYSTERY).isNotNull();
            assertThat(BookGenre.THRILLER).isNotNull();
            assertThat(BookGenre.HORROR).isNotNull();
            assertThat(BookGenre.ROMANCE).isNotNull();
            assertThat(BookGenre.HISTORICAL_FICTION).isNotNull();
        }

        @Test
        @DisplayName("모든 인문/사회 장르가 존재한다")
        void humanitiesGenresExist() {
            // when & then
            assertThat(BookGenre.PHILOSOPHY).isNotNull();
            assertThat(BookGenre.PSYCHOLOGY).isNotNull();
            assertThat(BookGenre.SOCIOLOGY).isNotNull();
            assertThat(BookGenre.POLITICS).isNotNull();
            assertThat(BookGenre.ECONOMICS).isNotNull();
            assertThat(BookGenre.HISTORY).isNotNull();
        }

        @Test
        @DisplayName("모든 자기계발/실용 장르가 존재한다")
        void selfHelpGenresExist() {
            // when & then
            assertThat(BookGenre.SELF_HELP).isNotNull();
            assertThat(BookGenre.BUSINESS).isNotNull();
            assertThat(BookGenre.LEADERSHIP).isNotNull();
            assertThat(BookGenre.CAREER).isNotNull();
            assertThat(BookGenre.FINANCE).isNotNull();
        }

        @Test
        @DisplayName("모든 과학/기술 장르가 존재한다")
        void scienceGenresExist() {
            // when & then
            assertThat(BookGenre.SCIENCE).isNotNull();
            assertThat(BookGenre.TECHNOLOGY).isNotNull();
            assertThat(BookGenre.MATHEMATICS).isNotNull();
            assertThat(BookGenre.MEDICINE).isNotNull();
        }

        @Test
        @DisplayName("모든 예술/문화 장르가 존재한다")
        void artGenresExist() {
            // when & then
            assertThat(BookGenre.ART).isNotNull();
            assertThat(BookGenre.MUSIC).isNotNull();
            assertThat(BookGenre.CULTURE).isNotNull();
        }

        @Test
        @DisplayName("기타 장르가 존재한다")
        void otherGenresExist() {
            // when & then
            assertThat(BookGenre.TRAVEL).isNotNull();
            assertThat(BookGenre.COOKING).isNotNull();
            assertThat(BookGenre.HEALTH).isNotNull();
            assertThat(BookGenre.RELIGION).isNotNull();
            assertThat(BookGenre.EDUCATION).isNotNull();
            assertThat(BookGenre.CHILDREN).isNotNull();
            assertThat(BookGenre.POCKET).isNotNull();
        }
    }

    @Nested
    @DisplayName("BookGenre displayName")
    class BookGenreDisplayName {

        @Test
        @DisplayName("각 장르는 한국어 표시 이름을 가진다")
        void hasKoreanDisplayName() {
            // when & then
            assertThat(BookGenre.FICTION.displayName()).isEqualTo("문학 / 소설");
            assertThat(BookGenre.FANTASY.displayName()).isEqualTo("판타지");
            assertThat(BookGenre.PHILOSOPHY.displayName()).isEqualTo("철학");
            assertThat(BookGenre.SELF_HELP.displayName()).isEqualTo("자기계발");
            assertThat(BookGenre.SCIENCE.displayName()).isEqualTo("과학");
            assertThat(BookGenre.TECHNOLOGY.displayName()).isEqualTo("기술 / 프로그래밍");
        }

        @Test
        @DisplayName("모든 장르의 displayName은 null이 아니다")
        void allDisplayNamesNotNull() {
            // when & then
            for (BookGenre genre : BookGenre.values()) {
                assertThat(genre.displayName()).isNotNull();
                assertThat(genre.displayName()).isNotBlank();
            }
        }
    }

    @Nested
    @DisplayName("BookGenre from 메서드")
    class BookGenreFrom {

        @Test
        @DisplayName("소문자 문자열로 BookGenre를 생성할 수 있다")
        void createFromLowercase() {
            // when
            BookGenre genre = BookGenre.from("fiction");

            // then
            assertThat(genre).isEqualTo(BookGenre.FICTION);
        }

        @Test
        @DisplayName("대문자 문자열로 BookGenre를 생성할 수 있다")
        void createFromUppercase() {
            // when
            BookGenre genre = BookGenre.from("FANTASY");

            // then
            assertThat(genre).isEqualTo(BookGenre.FANTASY);
        }

        @Test
        @DisplayName("대소문자 혼합 문자열로도 생성할 수 있다")
        void createFromMixedCase() {
            // when
            BookGenre genre1 = BookGenre.from("FiCtIoN");
            BookGenre genre2 = BookGenre.from("ScIeNcE_fIcTiOn");

            // then
            assertThat(genre1).isEqualTo(BookGenre.FICTION);
            assertThat(genre2).isEqualTo(BookGenre.SCIENCE_FICTION);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백 문자열은 null을 반환한다")
        void nullOrBlankReturnsNull(String blankValue) {
            // when
            BookGenre genre = BookGenre.from(blankValue);

            // then
            assertThat(genre).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 값으로 생성 시 예외가 발생한다")
        void createWithInvalidValue() {
            // when & then
            assertThatThrownBy(() -> BookGenre.from("INVALID_GENRE"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid book genre: INVALID_GENRE");
        }

        @Test
        @DisplayName("언더스코어가 포함된 장르도 생성할 수 있다")
        void createGenreWithUnderscore() {
            // when
            BookGenre genre1 = BookGenre.from("SCIENCE_FICTION");
            BookGenre genre2 = BookGenre.from("HISTORICAL_FICTION");
            BookGenre genre3 = BookGenre.from("SELF_HELP");

            // then
            assertThat(genre1).isEqualTo(BookGenre.SCIENCE_FICTION);
            assertThat(genre2).isEqualTo(BookGenre.HISTORICAL_FICTION);
            assertThat(genre3).isEqualTo(BookGenre.SELF_HELP);
        }
    }

    @Nested
    @DisplayName("BookGenre 동등성")
    class BookGenreEquality {

        @Test
        @DisplayName("같은 BookGenre 값은 동등하다")
        void sameGenresAreEqual() {
            // given
            BookGenre genre1 = BookGenre.FICTION;
            BookGenre genre2 = BookGenre.FICTION;

            // when & then
            assertThat(genre1).isEqualTo(genre2);
            assertThat(genre1).isSameAs(genre2);
        }

        @Test
        @DisplayName("다른 BookGenre 값은 동등하지 않다")
        void differentGenresAreNotEqual() {
            // given
            BookGenre genre1 = BookGenre.FICTION;
            BookGenre genre2 = BookGenre.FANTASY;

            // when & then
            assertThat(genre1).isNotEqualTo(genre2);
        }

        @Test
        @DisplayName("from 메서드로 생성한 장르는 동일한 인스턴스이다")
        void fromMethodReturnsSameInstance() {
            // when
            BookGenre genre1 = BookGenre.from("fiction");
            BookGenre genre2 = BookGenre.FICTION;

            // then
            assertThat(genre1).isSameAs(genre2);
        }
    }

    @Nested
    @DisplayName("BookGenre 특정 장르 테스트")
    class SpecificGenreTests {

        @Test
        @DisplayName("SCIENCE_FICTION 장르를 정확히 생성할 수 있다")
        void createScienceFiction() {
            // when
            BookGenre genre = BookGenre.from("science_fiction");

            // then
            assertThat(genre).isEqualTo(BookGenre.SCIENCE_FICTION);
            assertThat(genre.displayName()).isEqualTo("SF");
        }

        @Test
        @DisplayName("TECHNOLOGY 장르의 표시 이름에는 슬래시가 포함된다")
        void technologyDisplayName() {
            // when
            BookGenre genre = BookGenre.TECHNOLOGY;

            // then
            assertThat(genre.displayName()).contains("/");
            assertThat(genre.displayName()).isEqualTo("기술 / 프로그래밍");
        }

        @Test
        @DisplayName("POCKET 장르는 기타 장르를 나타낸다")
        void pocketGenreIsOther() {
            // when
            BookGenre genre = BookGenre.POCKET;

            // then
            assertThat(genre.displayName()).isEqualTo("단행본 / 기타");
        }
    }
}
