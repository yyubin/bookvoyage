package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewReaction 도메인 테스트")
class ReviewReactionTest {

    @Nested
    @DisplayName("ReviewReaction 생성 - of 메서드")
    class CreateReviewReactionWithOf {

        @Test
        @DisplayName("유효한 데이터로 ReviewReaction을 생성할 수 있다")
        void createWithValidData() {
            // given
            ReviewReactionId id = ReviewReactionId.of(1L);
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";
            LocalDateTime createdAt = LocalDateTime.now();

            // when
            ReviewReaction reaction = ReviewReaction.of(id, reviewId, userId, content, createdAt);

            // then
            assertThat(reaction).isNotNull();
            assertThat(reaction.getId()).isEqualTo(id);
            assertThat(reaction.getReviewId()).isEqualTo(reviewId);
            assertThat(reaction.getUserId()).isEqualTo(userId);
            assertThat(reaction.getContent()).isEqualTo(content);
            assertThat(reaction.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("null ID로 ReviewReaction을 생성할 수 있다")
        void createWithNullId() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";
            LocalDateTime createdAt = LocalDateTime.now();

            // when
            ReviewReaction reaction = ReviewReaction.of(null, reviewId, userId, content, createdAt);

            // then
            assertThat(reaction).isNotNull();
            assertThat(reaction.getId()).isNull();
        }

        @Test
        @DisplayName("null reviewId로 생성 시 예외가 발생한다")
        void createWithNullReviewId() {
            // given
            ReviewReactionId id = ReviewReactionId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> ReviewReaction.of(id, null, userId, content, createdAt))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Review ID cannot be null");
        }

        @Test
        @DisplayName("null userId로 생성 시 예외가 발생한다")
        void createWithNullUserId() {
            // given
            ReviewReactionId id = ReviewReactionId.of(1L);
            ReviewId reviewId = ReviewId.of(1L);
            String content = "👍";
            LocalDateTime createdAt = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> ReviewReaction.of(id, reviewId, null, content, createdAt))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID cannot be null");
        }

        @Test
        @DisplayName("null createdAt로 생성 시 예외가 발생한다")
        void createWithNullCreatedAt() {
            // given
            ReviewReactionId id = ReviewReactionId.of(1L);
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";

            // when & then
            assertThatThrownBy(() -> ReviewReaction.of(id, reviewId, userId, content, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Created at cannot be null");
        }
    }

    @Nested
    @DisplayName("ReviewReaction 생성 - create 메서드")
    class CreateReviewReactionWithCreate {

        @Test
        @DisplayName("create 메서드로 ReviewReaction을 생성할 수 있다")
        void createWithValidData() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";

            // when
            ReviewReaction reaction = ReviewReaction.create(reviewId, userId, content);

            // then
            assertThat(reaction).isNotNull();
            assertThat(reaction.getId()).isNull();
            assertThat(reaction.getReviewId()).isEqualTo(reviewId);
            assertThat(reaction.getUserId()).isEqualTo(userId);
            assertThat(reaction.getContent()).isEqualTo(content);
            assertThat(reaction.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("create 메서드는 현재 시간을 createdAt으로 설정한다")
        void createSetsCurrentTime() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";
            LocalDateTime before = LocalDateTime.now();

            // when
            ReviewReaction reaction = ReviewReaction.create(reviewId, userId, content);

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(reaction.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(reaction.getCreatedAt()).isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("ReviewReaction content 유효성 검증")
    class ValidateContent {

        @Test
        @DisplayName("최대 길이(32자) 이하의 content로 생성할 수 있다")
        void createWithMaxLengthContent() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "a".repeat(32);

            // when
            ReviewReaction reaction = ReviewReaction.create(reviewId, userId, content);

            // then
            assertThat(reaction.getContent()).hasSize(32);
        }

        @Test
        @DisplayName("최대 길이를 초과하는 content로 생성 시 예외가 발생한다")
        void createWithTooLongContent() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "a".repeat(33);

            // when & then
            assertThatThrownBy(() -> ReviewReaction.create(reviewId, userId, content))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reaction content cannot exceed 32 characters");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백만 있는 content로 생성 시 예외가 발생한다")
        void createWithBlankContent(String blankContent) {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);

            // when & then
            assertThatThrownBy(() -> ReviewReaction.create(reviewId, userId, blankContent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reaction content cannot be empty");
        }

        @Test
        @DisplayName("이모지 content로 생성할 수 있다")
        void createWithEmojiContent() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍😊🎉";

            // when
            ReviewReaction reaction = ReviewReaction.create(reviewId, userId, content);

            // then
            assertThat(reaction.getContent()).isEqualTo(content);
        }
    }

    @Nested
    @DisplayName("ReviewReaction 동등성")
    class ReviewReactionEquality {

        @Test
        @DisplayName("같은 ID를 가진 ReviewReaction은 동등하다")
        void equalReactionsWithSameId() {
            // given
            ReviewReactionId id = ReviewReactionId.of(1L);
            ReviewId reviewId1 = ReviewId.of(1L);
            ReviewId reviewId2 = ReviewId.of(2L);
            UserId userId1 = new UserId(1L);
            UserId userId2 = new UserId(2L);
            LocalDateTime now = LocalDateTime.now();

            ReviewReaction reaction1 = ReviewReaction.of(id, reviewId1, userId1, "👍", now);
            ReviewReaction reaction2 = ReviewReaction.of(id, reviewId2, userId2, "❤️", now);

            // when & then
            assertThat(reaction1).isEqualTo(reaction2);
            assertThat(reaction1.hashCode()).isEqualTo(reaction2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 ReviewReaction은 동등하지 않다")
        void notEqualReactionsWithDifferentId() {
            // given
            ReviewReactionId id1 = ReviewReactionId.of(1L);
            ReviewReactionId id2 = ReviewReactionId.of(2L);
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "👍";
            LocalDateTime now = LocalDateTime.now();

            ReviewReaction reaction1 = ReviewReaction.of(id1, reviewId, userId, content, now);
            ReviewReaction reaction2 = ReviewReaction.of(id2, reviewId, userId, content, now);

            // when & then
            assertThat(reaction1).isNotEqualTo(reaction2);
        }

        @Test
        @DisplayName("ReviewReaction은 자기 자신과 동등하다")
        void equalToItself() {
            // given
            ReviewReaction reaction = ReviewReaction.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "👍"
            );

            // when & then
            assertThat(reaction).isEqualTo(reaction);
        }

        @Test
        @DisplayName("ReviewReaction은 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            ReviewReaction reaction = ReviewReaction.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "👍"
            );

            // when & then
            assertThat(reaction).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("ReviewReaction toString")
    class ReviewReactionToString {

        @Test
        @DisplayName("toString()은 ReviewReaction 정보를 포함한 문자열을 반환한다")
        void toStringContainsReactionInfo() {
            // given
            ReviewReaction reaction = ReviewReaction.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "👍"
            );

            // when
            String result = reaction.toString();

            // then
            assertThat(result).contains("ReviewReaction");
            assertThat(result).contains("reviewId=");
            assertThat(result).contains("userId=");
            assertThat(result).contains("content=");
        }
    }
}
