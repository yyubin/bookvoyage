package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewComment 도메인 테스트")
class ReviewCommentTest {

    @Nested
    @DisplayName("ReviewComment 생성 - of 메서드")
    class CreateReviewCommentWithOf {

        @Test
        @DisplayName("유효한 데이터로 ReviewComment를 생성할 수 있다")
        void createWithValidData() {
            // given
            ReviewCommentId id = ReviewCommentId.of(1L);
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "Great review!";
            ReviewCommentId parentId = null;
            LocalDateTime createdAt = LocalDateTime.now();
            LocalDateTime editedAt = null;
            boolean deleted = false;
            List<Mention> mentions = Collections.emptyList();

            // when
            ReviewComment comment = ReviewComment.of(
                    id, reviewId, userId, content, parentId, createdAt, editedAt, deleted, mentions
            );

            // then
            assertThat(comment).isNotNull();
            assertThat(comment.getId()).isEqualTo(id);
            assertThat(comment.getReviewId()).isEqualTo(reviewId);
            assertThat(comment.getUserId()).isEqualTo(userId);
            assertThat(comment.getContent()).isEqualTo(content);
            assertThat(comment.getParentId()).isNull();
            assertThat(comment.getCreatedAt()).isEqualTo(createdAt);
            assertThat(comment.getEditedAt()).isNull();
            assertThat(comment.isDeleted()).isFalse();
            assertThat(comment.getMentions()).isEqualTo(mentions);
        }

        @Test
        @DisplayName("parentId를 가진 대댓글을 생성할 수 있다")
        void createReplyComment() {
            // given
            ReviewCommentId parentId = ReviewCommentId.of(1L);
            ReviewCommentId childId = ReviewCommentId.of(2L);

            // when
            ReviewComment comment = ReviewComment.of(
                    childId,
                    ReviewId.of(1L),
                    new UserId(1L),
                    "Reply comment",
                    parentId,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            );

            // then
            assertThat(comment.getParentId()).isEqualTo(parentId);
        }

        @Test
        @DisplayName("mentions 리스트는 불변이다")
        void mentionsListIsImmutable() {
            // given
            List<Mention> mentions = List.of(new Mention(1L, 0, 5));
            ReviewComment comment = ReviewComment.of(
                    ReviewCommentId.of(1L),
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content @user",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    mentions
            );

            // when & then
            assertThatThrownBy(() -> comment.getMentions().add(new Mention(2L, 0, 5)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("자기 자신을 parentId로 참조하면 예외가 발생한다")
        void createWithSelfReferencingParentId() {
            // given
            ReviewCommentId id = ReviewCommentId.of(1L);

            // when & then
            assertThatThrownBy(() -> ReviewComment.of(
                    id,
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    id,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment cannot reference itself as parent");
        }
    }

    @Nested
    @DisplayName("ReviewComment 생성 - create 메서드")
    class CreateReviewCommentWithCreate {

        @Test
        @DisplayName("create 메서드로 ReviewComment를 생성할 수 있다")
        void createWithValidData() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            String content = "Great review!";
            ReviewCommentId parentId = null;
            List<Mention> mentions = Collections.emptyList();

            // when
            ReviewComment comment = ReviewComment.create(reviewId, userId, content, parentId, mentions);

            // then
            assertThat(comment).isNotNull();
            assertThat(comment.getId()).isNull();
            assertThat(comment.getReviewId()).isEqualTo(reviewId);
            assertThat(comment.getUserId()).isEqualTo(userId);
            assertThat(comment.getContent()).isEqualTo(content);
            assertThat(comment.getParentId()).isNull();
            assertThat(comment.getCreatedAt()).isNotNull();
            assertThat(comment.getEditedAt()).isNull();
            assertThat(comment.isDeleted()).isFalse();
            assertThat(comment.getMentions()).isEqualTo(mentions);
        }

        @Test
        @DisplayName("null mentions는 빈 리스트로 변환된다")
        void nullMentionsConvertedToEmptyList() {
            // when
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    null
            );

            // then
            assertThat(comment.getMentions()).isNotNull();
            assertThat(comment.getMentions()).isEmpty();
        }

        @Test
        @DisplayName("create 메서드는 현재 시간을 createdAt으로 설정한다")
        void createSetsCurrentTime() {
            // given
            LocalDateTime before = LocalDateTime.now();

            // when
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    Collections.emptyList()
            );

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(comment.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(comment.getCreatedAt()).isBeforeOrEqualTo(after);
        }
    }

    @Nested
    @DisplayName("ReviewComment content 유효성 검증")
    class ValidateContent {

        @Test
        @DisplayName("최대 길이(2000자) 이하의 content로 생성할 수 있다")
        void createWithMaxLengthContent() {
            // given
            String maxLengthContent = "a".repeat(2000);

            // when
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    maxLengthContent,
                    null,
                    Collections.emptyList()
            );

            // then
            assertThat(comment.getContent()).hasSize(2000);
        }

        @Test
        @DisplayName("최대 길이를 초과하는 content로 생성 시 예외가 발생한다")
        void createWithTooLongContent() {
            // given
            String tooLongContent = "a".repeat(2001);

            // when & then
            assertThatThrownBy(() -> ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    tooLongContent,
                    null,
                    Collections.emptyList()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment content cannot exceed 2000 characters");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백만 있는 content로 생성 시 예외가 발생한다")
        void createWithBlankContent(String blankContent) {
            // when & then
            assertThatThrownBy(() -> ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    blankContent,
                    null,
                    Collections.emptyList()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment content cannot be empty");
        }
    }

    @Nested
    @DisplayName("ReviewComment 필수 필드 검증")
    class ValidateRequiredFields {

        @Test
        @DisplayName("null reviewId로 생성 시 예외가 발생한다")
        void createWithNullReviewId() {
            // when & then
            assertThatThrownBy(() -> ReviewComment.of(
                    ReviewCommentId.of(1L),
                    null,
                    new UserId(1L),
                    "content",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Review ID cannot be null");
        }

        @Test
        @DisplayName("null userId로 생성 시 예외가 발생한다")
        void createWithNullUserId() {
            // when & then
            assertThatThrownBy(() -> ReviewComment.of(
                    ReviewCommentId.of(1L),
                    ReviewId.of(1L),
                    null,
                    "content",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID cannot be null");
        }

        @Test
        @DisplayName("null createdAt로 생성 시 예외가 발생한다")
        void createWithNullCreatedAt() {
            // when & then
            assertThatThrownBy(() -> ReviewComment.of(
                    ReviewCommentId.of(1L),
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    null,
                    null,
                    false,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Created at cannot be null");
        }

        @Test
        @DisplayName("null mentions로 생성 시 예외가 발생한다")
        void createWithNullMentions() {
            // when & then
            assertThatThrownBy(() -> ReviewComment.of(
                    ReviewCommentId.of(1L),
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Mentions cannot be null");
        }
    }

    @Nested
    @DisplayName("ReviewComment 업데이트 메서드")
    class UpdateReviewComment {

        @Test
        @DisplayName("updateContent로 내용을 업데이트할 수 있다")
        void updateContent() {
            // given
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "original content",
                    null,
                    Collections.emptyList()
            );
            String newContent = "updated content";
            List<Mention> newMentions = List.of(new Mention(1L, 0, 5));

            // when
            ReviewComment updated = comment.updateContent(newContent, newMentions);

            // then
            assertThat(updated.getContent()).isEqualTo(newContent);
            assertThat(updated.getMentions()).isEqualTo(newMentions);
            assertThat(updated.getEditedAt()).isNotNull();
            assertThat(comment.getContent()).isEqualTo("original content");
            assertThat(comment.getEditedAt()).isNull();
        }

        @Test
        @DisplayName("updateContent 시 editedAt이 설정된다")
        void updateContentSetsEditedAt() {
            // given
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "original content",
                    null,
                    Collections.emptyList()
            );
            LocalDateTime before = LocalDateTime.now();

            // when
            ReviewComment updated = comment.updateContent("new content", Collections.emptyList());

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(updated.getEditedAt()).isAfterOrEqualTo(before);
            assertThat(updated.getEditedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("markDeleted로 삭제 상태로 변경할 수 있다")
        void markDeleted() {
            // given
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    Collections.emptyList()
            );

            // when
            ReviewComment deleted = comment.markDeleted();

            // then
            assertThat(deleted.isDeleted()).isTrue();
            assertThat(comment.isDeleted()).isFalse();
        }
    }

    @Nested
    @DisplayName("ReviewComment 비즈니스 메서드")
    class ReviewCommentBusinessMethods {

        @Test
        @DisplayName("isOwnedBy는 소유자를 확인한다")
        void isOwnedBy() {
            // given
            UserId userId = new UserId(1L);
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    userId,
                    "content",
                    null,
                    Collections.emptyList()
            );

            // when & then
            assertThat(comment.isOwnedBy(userId)).isTrue();
            assertThat(comment.isOwnedBy(new UserId(2L))).isFalse();
        }
    }

    @Nested
    @DisplayName("ReviewComment 동등성")
    class ReviewCommentEquality {

        @Test
        @DisplayName("같은 ID를 가진 ReviewComment는 동등하다")
        void equalCommentsWithSameId() {
            // given
            ReviewCommentId id = ReviewCommentId.of(1L);
            ReviewComment comment1 = ReviewComment.of(
                    id,
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content1",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            );
            ReviewComment comment2 = ReviewComment.of(
                    id,
                    ReviewId.of(2L),
                    new UserId(2L),
                    "content2",
                    null,
                    LocalDateTime.now(),
                    null,
                    true,
                    Collections.emptyList()
            );

            // when & then
            assertThat(comment1).isEqualTo(comment2);
            assertThat(comment1.hashCode()).isEqualTo(comment2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 ReviewComment는 동등하지 않다")
        void notEqualCommentsWithDifferentId() {
            // given
            ReviewComment comment1 = ReviewComment.of(
                    ReviewCommentId.of(1L),
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            );
            ReviewComment comment2 = ReviewComment.of(
                    ReviewCommentId.of(2L),
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    LocalDateTime.now(),
                    null,
                    false,
                    Collections.emptyList()
            );

            // when & then
            assertThat(comment1).isNotEqualTo(comment2);
        }

        @Test
        @DisplayName("ReviewComment는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    Collections.emptyList()
            );

            // when & then
            assertThat(comment).isEqualTo(comment);
        }

        @Test
        @DisplayName("ReviewComment는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    Collections.emptyList()
            );

            // when & then
            assertThat(comment).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("ReviewComment toString")
    class ReviewCommentToString {

        @Test
        @DisplayName("toString()은 ReviewComment 정보를 포함한 문자열을 반환한다")
        void toStringContainsCommentInfo() {
            // given
            ReviewComment comment = ReviewComment.create(
                    ReviewId.of(1L),
                    new UserId(1L),
                    "content",
                    null,
                    Collections.emptyList()
            );

            // when
            String result = comment.toString();

            // then
            assertThat(result).contains("ReviewComment");
            assertThat(result).contains("reviewId=");
            assertThat(result).contains("userId=");
            assertThat(result).contains("content=");
        }
    }
}
