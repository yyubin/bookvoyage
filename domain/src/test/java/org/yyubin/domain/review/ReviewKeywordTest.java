package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewKeyword 도메인 테스트")
class ReviewKeywordTest {

    @Nested
    @DisplayName("ReviewKeyword 생성")
    class CreateReviewKeyword {

        @Test
        @DisplayName("유효한 ReviewId와 KeywordId로 ReviewKeyword를 생성할 수 있다")
        void createWithValidIds() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            KeywordId keywordId = new KeywordId(1L);

            // when
            ReviewKeyword reviewKeyword = new ReviewKeyword(reviewId, keywordId);

            // then
            assertThat(reviewKeyword).isNotNull();
            assertThat(reviewKeyword.reviewId()).isEqualTo(reviewId);
            assertThat(reviewKeyword.keywordId()).isEqualTo(keywordId);
        }

        @Test
        @DisplayName("null ReviewId로 ReviewKeyword를 생성할 수 있다")
        void createWithNullReviewId() {
            // given
            KeywordId keywordId = new KeywordId(1L);

            // when
            ReviewKeyword reviewKeyword = new ReviewKeyword(null, keywordId);

            // then
            assertThat(reviewKeyword).isNotNull();
            assertThat(reviewKeyword.reviewId()).isNull();
            assertThat(reviewKeyword.keywordId()).isEqualTo(keywordId);
        }

        @Test
        @DisplayName("null KeywordId로 ReviewKeyword를 생성할 수 있다")
        void createWithNullKeywordId() {
            // given
            ReviewId reviewId = ReviewId.of(1L);

            // when
            ReviewKeyword reviewKeyword = new ReviewKeyword(reviewId, null);

            // then
            assertThat(reviewKeyword).isNotNull();
            assertThat(reviewKeyword.reviewId()).isEqualTo(reviewId);
            assertThat(reviewKeyword.keywordId()).isNull();
        }
    }

    @Nested
    @DisplayName("ReviewKeyword 동등성")
    class ReviewKeywordEquality {

        @Test
        @DisplayName("같은 ReviewId와 KeywordId를 가진 ReviewKeyword는 동등하다")
        void equalReviewKeywordsWithSameIds() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            KeywordId keywordId = new KeywordId(1L);
            ReviewKeyword reviewKeyword1 = new ReviewKeyword(reviewId, keywordId);
            ReviewKeyword reviewKeyword2 = new ReviewKeyword(reviewId, keywordId);

            // when & then
            assertThat(reviewKeyword1).isEqualTo(reviewKeyword2);
            assertThat(reviewKeyword1.hashCode()).isEqualTo(reviewKeyword2.hashCode());
        }

        @Test
        @DisplayName("다른 ReviewId를 가진 ReviewKeyword는 동등하지 않다")
        void notEqualReviewKeywordsWithDifferentReviewId() {
            // given
            ReviewId reviewId1 = ReviewId.of(1L);
            ReviewId reviewId2 = ReviewId.of(2L);
            KeywordId keywordId = new KeywordId(1L);
            ReviewKeyword reviewKeyword1 = new ReviewKeyword(reviewId1, keywordId);
            ReviewKeyword reviewKeyword2 = new ReviewKeyword(reviewId2, keywordId);

            // when & then
            assertThat(reviewKeyword1).isNotEqualTo(reviewKeyword2);
        }

        @Test
        @DisplayName("다른 KeywordId를 가진 ReviewKeyword는 동등하지 않다")
        void notEqualReviewKeywordsWithDifferentKeywordId() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            KeywordId keywordId1 = new KeywordId(1L);
            KeywordId keywordId2 = new KeywordId(2L);
            ReviewKeyword reviewKeyword1 = new ReviewKeyword(reviewId, keywordId1);
            ReviewKeyword reviewKeyword2 = new ReviewKeyword(reviewId, keywordId2);

            // when & then
            assertThat(reviewKeyword1).isNotEqualTo(reviewKeyword2);
        }

        @Test
        @DisplayName("ReviewKeyword는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            ReviewKeyword reviewKeyword = new ReviewKeyword(
                    ReviewId.of(1L),
                    new KeywordId(1L)
            );

            // when & then
            assertThat(reviewKeyword).isEqualTo(reviewKeyword);
        }

        @Test
        @DisplayName("ReviewKeyword는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            ReviewKeyword reviewKeyword = new ReviewKeyword(
                    ReviewId.of(1L),
                    new KeywordId(1L)
            );

            // when & then
            assertThat(reviewKeyword).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("ReviewKeyword toString")
    class ReviewKeywordToString {

        @Test
        @DisplayName("toString()은 ReviewKeyword 정보를 포함한 문자열을 반환한다")
        void toStringContainsReviewKeywordInfo() {
            // given
            ReviewKeyword reviewKeyword = new ReviewKeyword(
                    ReviewId.of(1L),
                    new KeywordId(2L)
            );

            // when
            String result = reviewKeyword.toString();

            // then
            assertThat(result).contains("ReviewKeyword");
            assertThat(result).contains("reviewId=");
            assertThat(result).contains("keywordId=");
        }
    }

    @Nested
    @DisplayName("ReviewKeyword 필드 접근")
    class ReviewKeywordFieldAccess {

        @Test
        @DisplayName("reviewId()와 keywordId() 메서드로 값에 접근할 수 있다")
        void accessFields() {
            // given
            ReviewId expectedReviewId = ReviewId.of(10L);
            KeywordId expectedKeywordId = new KeywordId(20L);
            ReviewKeyword reviewKeyword = new ReviewKeyword(expectedReviewId, expectedKeywordId);

            // when
            ReviewId actualReviewId = reviewKeyword.reviewId();
            KeywordId actualKeywordId = reviewKeyword.keywordId();

            // then
            assertThat(actualReviewId).isEqualTo(expectedReviewId);
            assertThat(actualKeywordId).isEqualTo(expectedKeywordId);
        }
    }
}
