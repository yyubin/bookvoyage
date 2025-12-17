package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Review 도메인 테스트")
class ReviewTest {

    @Nested
    @DisplayName("Review 생성 - of 메서드")
    class CreateReviewWithOf {

        @Test
        @DisplayName("유효한 데이터로 Review를 생성할 수 있다")
        void createWithValidData() {
            // given
            ReviewId id = ReviewId.of(1L);
            UserId userId = new UserId(1L);
            BookId bookId = BookId.of(1L);
            Rating rating = Rating.of(5);
            String content = "Great book!";
            LocalDateTime createdAt = LocalDateTime.now();
            ReviewVisibility visibility = ReviewVisibility.PUBLIC;
            boolean deleted = false;
            long viewCount = 100L;
            BookGenre genre = BookGenre.FICTION;
            List<Mention> mentions = Collections.emptyList();

            // when
            Review review = Review.of(id, userId, bookId, rating, content, createdAt, visibility, deleted, viewCount, genre, mentions);

            // then
            assertThat(review).isNotNull();
            assertThat(review.getId()).isEqualTo(id);
            assertThat(review.getUserId()).isEqualTo(userId);
            assertThat(review.getBookId()).isEqualTo(bookId);
            assertThat(review.getRating()).isEqualTo(rating);
            assertThat(review.getContent()).isEqualTo(content);
            assertThat(review.getCreatedAt()).isEqualTo(createdAt);
            assertThat(review.getVisibility()).isEqualTo(visibility);
            assertThat(review.isDeleted()).isEqualTo(deleted);
            assertThat(review.getViewCount()).isEqualTo(viewCount);
            assertThat(review.getGenre()).isEqualTo(genre);
            assertThat(review.getMentions()).isEqualTo(mentions);
        }

        @Test
        @DisplayName("null content는 빈 문자열로 변환된다")
        void nullContentConvertedToEmptyString() {
            // when
            Review review = Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    null,
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // then
            assertThat(review.getContent()).isNotNull();
            assertThat(review.getContent()).isEmpty();
        }

        @Test
        @DisplayName("음수 viewCount는 0으로 변환된다")
        void negativeViewCountConvertedToZero() {
            // when
            Review review = Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    -100,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // then
            assertThat(review.getViewCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("mentions 리스트는 불변이다")
        void mentionsListIsImmutable() {
            // given
            List<Mention> mentions = List.of(new Mention(1L, 0, 5));
            Review review = Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    mentions
            );

            // when & then
            assertThatThrownBy(() -> review.getMentions().add(new Mention(2L, 0, 5)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Review 생성 - create 메서드")
    class CreateReviewWithCreate {

        @Test
        @DisplayName("create 메서드로 Review를 생성할 수 있다")
        void createWithValidData() {
            // given
            UserId userId = new UserId(1L);
            BookId bookId = BookId.of(1L);
            Rating rating = Rating.of(5);
            String content = "Great book!";
            ReviewVisibility visibility = ReviewVisibility.PUBLIC;
            BookGenre genre = BookGenre.FICTION;
            List<Mention> mentions = Collections.emptyList();

            // when
            Review review = Review.create(userId, bookId, rating, content, visibility, genre, mentions);

            // then
            assertThat(review).isNotNull();
            assertThat(review.getId()).isNull();
            assertThat(review.getUserId()).isEqualTo(userId);
            assertThat(review.getBookId()).isEqualTo(bookId);
            assertThat(review.getRating()).isEqualTo(rating);
            assertThat(review.getContent()).isEqualTo(content);
            assertThat(review.getVisibility()).isEqualTo(visibility);
            assertThat(review.isDeleted()).isFalse();
            assertThat(review.getViewCount()).isEqualTo(0);
            assertThat(review.getGenre()).isEqualTo(genre);
            assertThat(review.getMentions()).isEqualTo(mentions);
        }

        @Test
        @DisplayName("null visibility는 PUBLIC으로 기본 설정된다")
        void nullVisibilityDefaultsToPublic() {
            // when
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    null,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // then
            assertThat(review.getVisibility()).isEqualTo(ReviewVisibility.PUBLIC);
        }

        @Test
        @DisplayName("null mentions는 빈 리스트로 변환된다")
        void nullMentionsConvertedToEmptyList() {
            // when
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    null
            );

            // then
            assertThat(review.getMentions()).isNotNull();
            assertThat(review.getMentions()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Review 유효성 검증")
    class ValidateReview {

        @Test
        @DisplayName("5000자를 초과하는 content로 생성 시 예외가 발생한다")
        void createWithTooLongContent() {
            // given
            String tooLongContent = "a".repeat(5001);

            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    tooLongContent,
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Review content cannot exceed 5000 characters");
        }

        @Test
        @DisplayName("5000자 이하의 content로 생성할 수 있다")
        void createWithMaxLengthContent() {
            // given
            String maxLengthContent = "a".repeat(5000);

            // when
            Review review = Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    maxLengthContent,
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // then
            assertThat(review.getContent()).hasSize(5000);
        }

        @Test
        @DisplayName("null userId로 생성 시 예외가 발생한다")
        void createWithNullUserId() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    null,
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("User ID cannot be null");
        }

        @Test
        @DisplayName("null bookId로 생성 시 예외가 발생한다")
        void createWithNullBookId() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    null,
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Book ID cannot be null");
        }

        @Test
        @DisplayName("null rating으로 생성 시 예외가 발생한다")
        void createWithNullRating() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    null,
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Rating cannot be null");
        }

        @Test
        @DisplayName("null createdAt로 생성 시 예외가 발생한다")
        void createWithNullCreatedAt() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    null,
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Created at cannot be null");
        }

        @Test
        @DisplayName("null visibility로 생성 시 예외가 발생한다")
        void createWithNullVisibility() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    null,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Visibility cannot be null");
        }

        @Test
        @DisplayName("null genre로 생성 시 예외가 발생한다")
        void createWithNullGenre() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    null,
                    Collections.emptyList()
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Genre cannot be null");
        }

        @Test
        @DisplayName("null mentions로 생성 시 예외가 발생한다")
        void createWithNullMentions() {
            // when & then
            assertThatThrownBy(() -> Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Mentions cannot be null");
        }
    }

    @Nested
    @DisplayName("Review 업데이트 메서드")
    class UpdateReview {

        @Test
        @DisplayName("updateContent로 내용을 업데이트할 수 있다")
        void updateContent() {
            // given
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "original content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );
            String newContent = "updated content";
            List<Mention> newMentions = List.of(new Mention(1L, 0, 5));

            // when
            Review updated = review.updateContent(newContent, newMentions);

            // then
            assertThat(updated.getContent()).isEqualTo(newContent);
            assertThat(updated.getMentions()).isEqualTo(newMentions);
            assertThat(review.getContent()).isEqualTo("original content");
        }

        @Test
        @DisplayName("updateRating으로 평점을 업데이트할 수 있다")
        void updateRating() {
            // given
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(3),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );
            Rating newRating = Rating.of(5);

            // when
            Review updated = review.updateRating(newRating);

            // then
            assertThat(updated.getRating()).isEqualTo(newRating);
            assertThat(review.getRating()).isEqualTo(Rating.of(3));
        }

        @Test
        @DisplayName("updateVisibility로 공개 설정을 업데이트할 수 있다")
        void updateVisibility() {
            // given
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );
            ReviewVisibility newVisibility = ReviewVisibility.PRIVATE;

            // when
            Review updated = review.updateVisibility(newVisibility);

            // then
            assertThat(updated.getVisibility()).isEqualTo(newVisibility);
            assertThat(review.getVisibility()).isEqualTo(ReviewVisibility.PUBLIC);
        }

        @Test
        @DisplayName("updateGenre로 장르를 업데이트할 수 있다")
        void updateGenre() {
            // given
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );
            BookGenre newGenre = BookGenre.FANTASY;

            // when
            Review updated = review.updateGenre(newGenre);

            // then
            assertThat(updated.getGenre()).isEqualTo(newGenre);
            assertThat(review.getGenre()).isEqualTo(BookGenre.FICTION);
        }

        @Test
        @DisplayName("markDeleted로 삭제 상태로 변경할 수 있다")
        void markDeleted() {
            // given
            Review review = Review.create(
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // when
            Review deleted = review.markDeleted();

            // then
            assertThat(deleted.isDeleted()).isTrue();
            assertThat(review.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("increaseViewCount로 조회수를 증가시킬 수 있다")
        void increaseViewCount() {
            // given
            Review review = Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    100,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // when
            Review increased = review.increaseViewCount();

            // then
            assertThat(increased.getViewCount()).isEqualTo(101);
            assertThat(review.getViewCount()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Review 비즈니스 메서드")
    class ReviewBusinessMethods {

        @Test
        @DisplayName("isWrittenBy는 작성자를 확인한다")
        void isWrittenBy() {
            // given
            UserId userId = new UserId(1L);
            Review review = Review.create(
                    userId,
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // when & then
            assertThat(review.isWrittenBy(userId)).isTrue();
            assertThat(review.isWrittenBy(new UserId(2L))).isFalse();
        }

        @Test
        @DisplayName("isAboutBook은 책을 확인한다")
        void isAboutBook() {
            // given
            BookId bookId = BookId.of(1L);
            Review review = Review.create(
                    new UserId(1L),
                    bookId,
                    Rating.of(5),
                    "content",
                    ReviewVisibility.PUBLIC,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // when & then
            assertThat(review.isAboutBook(bookId)).isTrue();
            assertThat(review.isAboutBook(BookId.of(2L))).isFalse();
        }
    }

    @Nested
    @DisplayName("Review 동등성")
    class ReviewEquality {

        @Test
        @DisplayName("같은 ID를 가진 Review는 동등하다")
        void equalReviewsWithSameId() {
            // given
            ReviewId id = ReviewId.of(1L);
            Review review1 = Review.of(
                    id,
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content1",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );
            Review review2 = Review.of(
                    id,
                    new UserId(2L),
                    BookId.of(2L),
                    Rating.of(3),
                    "content2",
                    LocalDateTime.now(),
                    ReviewVisibility.PRIVATE,
                    true,
                    100,
                    BookGenre.FANTASY,
                    Collections.emptyList()
            );

            // when & then
            assertThat(review1).isEqualTo(review2);
            assertThat(review1.hashCode()).isEqualTo(review2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 Review는 동등하지 않다")
        void notEqualReviewsWithDifferentId() {
            // given
            Review review1 = Review.of(
                    ReviewId.of(1L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );
            Review review2 = Review.of(
                    ReviewId.of(2L),
                    new UserId(1L),
                    BookId.of(1L),
                    Rating.of(5),
                    "content",
                    LocalDateTime.now(),
                    ReviewVisibility.PUBLIC,
                    false,
                    0,
                    BookGenre.FICTION,
                    Collections.emptyList()
            );

            // when & then
            assertThat(review1).isNotEqualTo(review2);
        }
    }
}
