package org.yyubin.infrastructure.persistence.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class UserBookEntityTest {

    @Test
    @DisplayName("UserBook 도메인에서 엔티티로 변환")
    void fromDomain() {
        // given
        UserBook userBook = UserBook.create(
                new UserId(1L),
                BookId.of(1L),
                ReadingStatus.WANT_TO_READ
        );

        // when
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);

        // then
        assertThat(entity.getUserId()).isEqualTo(1L);
        assertThat(entity.getBookId()).isEqualTo(1L);
        assertThat(entity.getStatus()).isEqualTo(ReadingStatus.WANT_TO_READ);
        assertThat(entity.getProgressPercentage()).isEqualTo(0);
        assertThat(entity.getPersonalRating()).isNull();
        assertThat(entity.getPersonalMemo()).isNull();
        assertThat(entity.getReadingCount()).isEqualTo(1);
        assertThat(entity.getStartDate()).isNull();
        assertThat(entity.getCompletionDate()).isNull();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("엔티티에서 UserBook 도메인으로 변환")
    void toDomain() {
        // given
        LocalDateTime now = LocalDateTime.now();
        UserBookEntity entity = UserBookEntity.builder()
                .id(1L)
                .userId(1L)
                .bookId(1L)
                .status(ReadingStatus.READING)
                .progressPercentage(50)
                .personalRating(4)
                .personalMemo("재미있는 책")
                .readingCount(1)
                .startDate(now)
                .completionDate(null)
                .deleted(false)
                .deletedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // when
        UserBook userBook = entity.toDomain();

        // then
        assertThat(userBook.getId()).isEqualTo(1L);
        assertThat(userBook.getUserId().value()).isEqualTo(1L);
        assertThat(userBook.getBookId().getValue()).isEqualTo(1L);
        assertThat(userBook.getStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(userBook.getProgress().getPercentage()).isEqualTo(50);
        assertThat(userBook.getRating().getValue()).isEqualTo(4);
        assertThat(userBook.getMemo().getContent()).isEqualTo("재미있는 책");
        assertThat(userBook.getReadingCount().getCount()).isEqualTo(1);
        assertThat(userBook.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제된 UserBook 변환")
    void fromDomain_deleted() {
        // given
        UserBook userBook = UserBook.create(
                new UserId(1L),
                BookId.of(1L),
                ReadingStatus.WANT_TO_READ
        ).delete();

        // when
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);

        // then
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("완독 상태 UserBook 변환")
    void fromDomain_completed() {
        // given
        UserBook userBook = UserBook.create(
                new UserId(1L),
                BookId.of(1L),
                ReadingStatus.WANT_TO_READ
        ).startReading()
                .updateProgress(100)
                .markAsCompleted();

        // when
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);

        // then
        assertThat(entity.getStatus()).isEqualTo(ReadingStatus.COMPLETED);
        assertThat(entity.getProgressPercentage()).isEqualTo(100);
        assertThat(entity.getStartDate()).isNotNull();
        assertThat(entity.getCompletionDate()).isNotNull();
    }

    @Test
    @DisplayName("재독 UserBook 변환")
    void fromDomain_reReading() {
        // given
        UserBook userBook = UserBook.create(
                new UserId(1L),
                BookId.of(1L),
                ReadingStatus.WANT_TO_READ
        ).startReading()
                .updateProgress(100)
                .markAsCompleted()
                .startReading(); // Re-reading

        // when
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);

        // then
        assertThat(entity.getStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(entity.getReadingCount()).isEqualTo(2);
        assertThat(entity.getProgressPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("평점과 메모가 있는 UserBook 변환")
    void fromDomain_withRatingAndMemo() {
        // given
        UserBook userBook = UserBook.create(
                new UserId(1L),
                BookId.of(1L),
                ReadingStatus.WANT_TO_READ
        ).rate(5)
                .updateMemo("훌륭한 책입니다");

        // when
        UserBookEntity entity = UserBookEntity.fromDomain(userBook);

        // then
        assertThat(entity.getPersonalRating()).isEqualTo(5);
        assertThat(entity.getPersonalMemo()).isEqualTo("훌륭한 책입니다");
    }

    @Test
    @DisplayName("양방향 변환 일관성")
    void bidirectionalConversion() {
        // given
        UserBook original = UserBook.create(
                new UserId(1L),
                BookId.of(1L),
                ReadingStatus.WANT_TO_READ
        ).startReading()
                .updateProgress(75)
                .rate(4)
                .updateMemo("읽는 중입니다");

        // when
        UserBookEntity entity = UserBookEntity.fromDomain(original);
        UserBook converted = entity.toDomain();

        // then
        assertThat(converted.getUserId().value()).isEqualTo(original.getUserId().value());
        assertThat(converted.getBookId().getValue()).isEqualTo(original.getBookId().getValue());
        assertThat(converted.getStatus()).isEqualTo(original.getStatus());
        assertThat(converted.getProgress().getPercentage()).isEqualTo(original.getProgress().getPercentage());
        assertThat(converted.getRating().getValue()).isEqualTo(original.getRating().getValue());
        assertThat(converted.getMemo().getContent()).isEqualTo(original.getMemo().getContent());
        assertThat(converted.getReadingCount().getCount()).isEqualTo(original.getReadingCount().getCount());
    }
}
