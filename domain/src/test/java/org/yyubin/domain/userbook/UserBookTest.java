package org.yyubin.domain.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

import static org.assertj.core.api.Assertions.*;

class UserBookTest {

    private static final UserId USER_ID = new UserId(1L);
    private static final BookId BOOK_ID = BookId.of(100L);

    @Test
    @DisplayName("UserBook 생성 - 기본 상태는 WANT_TO_READ")
    void create_withDefaultStatus() {
        // when
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.WANT_TO_READ);

        // then
        assertThat(userBook.getUserId()).isEqualTo(USER_ID);
        assertThat(userBook.getBookId()).isEqualTo(BOOK_ID);
        assertThat(userBook.getStatus()).isEqualTo(ReadingStatus.WANT_TO_READ);
        assertThat(userBook.getProgress().getPercentage()).isEqualTo(0);
        assertThat(userBook.getRating().hasRating()).isFalse();
        assertThat(userBook.getMemo().hasContent()).isFalse();
        assertThat(userBook.getReadingCount().getCount()).isEqualTo(1);
        assertThat(userBook.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("WANT_TO_READ → READING 상태 전환")
    void startReading_fromWantToRead() {
        // given
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.WANT_TO_READ);

        // when
        UserBook reading = userBook.startReading();

        // then
        assertThat(reading.getStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(reading.getStartDate()).isNotNull();
        assertThat(reading.getCompletionDate()).isNull();
        assertThat(reading.getReadingCount().getCount()).isEqualTo(1);
        assertThat(reading.getProgress().getPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("READING → COMPLETED 상태 전환")
    void markAsCompleted_fromReading() {
        // given
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING)
                .startReading();

        // when
        UserBook completed = userBook.markAsCompleted();

        // then
        assertThat(completed.getStatus()).isEqualTo(ReadingStatus.COMPLETED);
        assertThat(completed.getProgress().getPercentage()).isEqualTo(100);
        assertThat(completed.getCompletionDate()).isNotNull();
    }

    @Test
    @DisplayName("재독: COMPLETED → READING 전환 시 readingCount 증가")
    void startReading_fromCompleted_incrementsReadingCount() {
        // given
        UserBook completed = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING)
                .startReading()
                .markAsCompleted();

        // when
        UserBook reReading = completed.startReading();

        // then
        assertThat(reReading.getStatus()).isEqualTo(ReadingStatus.READING);
        assertThat(reReading.getReadingCount().getCount()).isEqualTo(2);
        assertThat(reReading.getProgress().getPercentage()).isEqualTo(0);
        assertThat(reReading.getCompletionDate()).isNull();
        assertThat(reReading.getStartDate()).isNotNull();
    }

    @Test
    @DisplayName("진행률 업데이트 - READING 상태일 때만 가능")
    void updateProgress_whenReading() {
        // given
        UserBook reading = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING)
                .startReading();

        // when
        UserBook updated = reading.updateProgress(50);

        // then
        assertThat(updated.getProgress().getPercentage()).isEqualTo(50);
    }

    @Test
    @DisplayName("진행률 업데이트 - READING 상태가 아니면 실패")
    void updateProgress_whenNotReading_throwsException() {
        // given
        UserBook wantToRead = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.WANT_TO_READ);

        // when & then
        assertThatThrownBy(() -> wantToRead.updateProgress(50))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Progress can only be updated when status is READING");
    }

    @Test
    @DisplayName("평점 등록")
    void rate() {
        // given
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.COMPLETED);

        // when
        UserBook rated = userBook.rate(5);

        // then
        assertThat(rated.getRating().getValue()).isEqualTo(5);
        assertThat(rated.hasRating()).isTrue();
    }

    @Test
    @DisplayName("메모 작성")
    void updateMemo() {
        // given
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING);
        String memo = "재미있는 책";

        // when
        UserBook updated = userBook.updateMemo(memo);

        // then
        assertThat(updated.getMemo().getContent()).isEqualTo(memo);
        assertThat(updated.hasMemo()).isTrue();
    }

    @Test
    @DisplayName("소프트 삭제")
    void delete() {
        // given
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING);

        // when
        UserBook deleted = userBook.delete();

        // then
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("삭제된 UserBook 복원")
    void restore() {
        // given
        UserBook deleted = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING)
                .delete();

        // when
        UserBook restored = deleted.restore();

        // then
        assertThat(restored.isDeleted()).isFalse();
        assertThat(restored.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("이미 삭제된 UserBook 재삭제 시 변경 없음")
    void delete_alreadyDeleted_noChange() {
        // given
        UserBook deleted = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING)
                .delete();

        // when
        UserBook reDeleted = deleted.delete();

        // then
        assertThat(reDeleted).isEqualTo(deleted);
    }

    @Test
    @DisplayName("이미 복원된 UserBook 재복원 시 변경 없음")
    void restore_alreadyRestored_noChange() {
        // given
        UserBook userBook = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.READING);

        // when
        UserBook restored = userBook.restore();

        // then
        assertThat(restored).isEqualTo(userBook);
    }

    @Test
    @DisplayName("상태 조회 메서드")
    void statusQueryMethods() {
        // given
        UserBook wantToRead = UserBook.create(USER_ID, BOOK_ID, ReadingStatus.WANT_TO_READ);
        UserBook reading = wantToRead.startReading();
        UserBook completed = reading.markAsCompleted();

        // then
        assertThat(wantToRead.isWantToRead()).isTrue();
        assertThat(wantToRead.isReading()).isFalse();
        assertThat(wantToRead.isCompleted()).isFalse();

        assertThat(reading.isWantToRead()).isFalse();
        assertThat(reading.isReading()).isTrue();
        assertThat(reading.isCompleted()).isFalse();
        assertThat(reading.canMarkCompleted()).isTrue();

        assertThat(completed.isWantToRead()).isFalse();
        assertThat(completed.isReading()).isFalse();
        assertThat(completed.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("필수 필드가 null이면 예외 발생")
    void create_withNullFields_throwsException() {
        assertThatThrownBy(() -> UserBook.create(null, BOOK_ID, ReadingStatus.READING))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("userId must not be null");

        assertThatThrownBy(() -> UserBook.create(USER_ID, null, ReadingStatus.READING))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("bookId must not be null");

        assertThatThrownBy(() -> UserBook.create(USER_ID, BOOK_ID, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("status must not be null");
    }
}
