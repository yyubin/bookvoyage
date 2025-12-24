package org.yyubin.application.userbook.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.application.userbook.command.AddUserBookCommand;
import org.yyubin.application.userbook.command.DeleteUserBookCommand;
import org.yyubin.application.userbook.command.UpdateUserBookProgressCommand;
import org.yyubin.application.userbook.command.UpdateUserBookStatusCommand;
import org.yyubin.application.userbook.dto.UserBookListResult;
import org.yyubin.application.userbook.dto.UserBookResult;
import org.yyubin.application.userbook.dto.UserBookStatisticsResult;
import org.yyubin.application.userbook.port.UserBookPort;
import org.yyubin.application.userbook.port.UserBookQueryPort;
import org.yyubin.application.userbook.query.GetUserBookStatisticsQuery;
import org.yyubin.application.userbook.query.GetUserBooksQuery;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.book.BookMetadata;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.domain.userbook.UserBook;

@ExtendWith(MockitoExtension.class)
class UserBookServiceTest {

    @Mock
    private UserBookPort userBookPort;

    @Mock
    private UserBookQueryPort userBookQueryPort;

    @Mock
    private LoadBookPort loadBookPort;

    @Mock
    private SaveBookPort saveBookPort;

    @InjectMocks
    private UserBookService userBookService;

    private UserId userId;
    private Book book;

    @BeforeEach
    void setUp() {
        userId = new UserId(1L);
        book = Book.of(BookId.of(10L), BookMetadata.of(
                "Test Book",
                List.of("Author"),
                "isbn10",
                "isbn13",
                "cover",
                "publisher",
                "2024-01-01",
                "description",
                "ko",
                320,
                "google"
        ));
    }

    @Test
    @DisplayName("서재 추가 - 기존 항목이 있으면 저장하지 않고 반환")
    void add_returnsExistingWhenPresent() {
        BookSearchItem item = BookSearchItem.of(
                "Test Book",
                List.of("Author"),
                "isbn10",
                "isbn13",
                "cover",
                "publisher",
                "2024-01-01",
                "description",
                "ko",
                320,
                "google"
        );
        UserBook existing = UserBook.of(
                5L,
                userId,
                book.getId(),
                ReadingStatus.WANT_TO_READ,
                org.yyubin.domain.userbook.ReadingProgress.notStarted(),
                org.yyubin.domain.userbook.PersonalRating.empty(),
                org.yyubin.domain.userbook.PersonalMemo.empty(),
                org.yyubin.domain.userbook.ReadingCount.first(),
                null,
                null,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(loadBookPort.loadByIdentifiers(item.getIsbn10(), item.getIsbn13(), item.getGoogleVolumeId()))
                .thenReturn(Optional.of(book));
        when(userBookPort.exists(userId, book.getId())).thenReturn(true);
        when(userBookPort.findByUserAndBook(userId, book.getId())).thenReturn(Optional.of(existing));

        UserBookResult result = userBookService.execute(new AddUserBookCommand(userId.value(), item, "WANT_TO_READ"));

        assertThat(result.userBookId()).isEqualTo(5L);
        assertThat(result.status()).isEqualTo(ReadingStatus.WANT_TO_READ);
        verify(userBookPort, never()).save(any(UserBook.class));
    }

    @Test
    @DisplayName("서재 추가 - 신규 항목 저장")
    void add_createsNewUserBook() {
        BookSearchItem item = BookSearchItem.of(
                "Test Book",
                List.of("Author"),
                "isbn10",
                "isbn13",
                "cover",
                "publisher",
                "2024-01-01",
                "description",
                "ko",
                320,
                "google"
        );
        UserBook created = UserBook.create(userId, book.getId(), ReadingStatus.WANT_TO_READ);
        UserBook saved = UserBook.of(
                1L,
                created.getUserId(),
                created.getBookId(),
                created.getStatus(),
                created.getProgress(),
                created.getRating(),
                created.getMemo(),
                created.getReadingCount(),
                created.getStartDate(),
                created.getCompletionDate(),
                created.isDeleted(),
                created.getDeletedAt(),
                created.getCreatedAt(),
                created.getUpdatedAt()
        );

        when(loadBookPort.loadByIdentifiers(item.getIsbn10(), item.getIsbn13(), item.getGoogleVolumeId()))
                .thenReturn(Optional.of(book));
        when(userBookPort.exists(userId, book.getId())).thenReturn(false);
        when(userBookPort.save(any(UserBook.class))).thenReturn(saved);

        UserBookResult result = userBookService.execute(new AddUserBookCommand(userId.value(), item, "WANT_TO_READ"));

        assertThat(result.userBookId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(ReadingStatus.WANT_TO_READ);
    }

    @Test
    @DisplayName("서재 조회 - 상태 필터 적용")
    void list_filtersByStatus() {
        UserBook reading = UserBook.create(userId, book.getId(), ReadingStatus.READING).startReading();

        when(userBookPort.findByUserAndStatus(userId, ReadingStatus.READING))
                .thenReturn(List.of(reading));
        when(loadBookPort.loadById(book.getId().getValue())).thenReturn(Optional.of(book));

        UserBookListResult result = userBookService.query(new GetUserBooksQuery(userId.value(), "READING"));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).status()).isEqualTo(ReadingStatus.READING);
    }

    @Test
    @DisplayName("상태 변경 - READING 전환")
    void updateStatus_toReading() {
        UserBook userBook = UserBook.create(userId, book.getId(), ReadingStatus.WANT_TO_READ);
        UserBook updated = userBook.startReading();

        when(userBookPort.findByUserAndBook(userId, book.getId())).thenReturn(Optional.of(userBook));
        when(userBookPort.save(any(UserBook.class))).thenReturn(updated);
        when(loadBookPort.loadById(book.getId().getValue())).thenReturn(Optional.of(book));

        UserBookResult result = userBookService.execute(
                new UpdateUserBookStatusCommand(userId.value(), book.getId().getValue(), "READING")
        );

        assertThat(result.status()).isEqualTo(ReadingStatus.READING);
        assertThat(result.progressPercentage()).isEqualTo(0);
    }

    @Test
    @DisplayName("진행률 업데이트")
    void updateProgress_updatesPercentage() {
        UserBook reading = UserBook.create(userId, book.getId(), ReadingStatus.READING).startReading();
        UserBook updated = reading.updateProgress(40);

        when(userBookPort.findByUserAndBook(userId, book.getId())).thenReturn(Optional.of(reading));
        when(userBookPort.save(any(UserBook.class))).thenReturn(updated);
        when(loadBookPort.loadById(book.getId().getValue())).thenReturn(Optional.of(book));

        UserBookResult result = userBookService.execute(
                new UpdateUserBookProgressCommand(userId.value(), book.getId().getValue(), 40)
        );

        assertThat(result.progressPercentage()).isEqualTo(40);
    }

    @Test
    @DisplayName("통계 조회")
    void statistics_countsByStatus() {
        when(userBookQueryPort.countByUser(userId)).thenReturn(10L);
        when(userBookQueryPort.countByUserAndStatus(userId, ReadingStatus.WANT_TO_READ)).thenReturn(3L);
        when(userBookQueryPort.countByUserAndStatus(userId, ReadingStatus.READING)).thenReturn(4L);
        when(userBookQueryPort.countByUserAndStatus(userId, ReadingStatus.COMPLETED)).thenReturn(3L);

        UserBookStatisticsResult result = userBookService.query(new GetUserBookStatisticsQuery(userId.value()));

        assertThat(result.totalCount()).isEqualTo(10L);
        assertThat(result.wantToReadCount()).isEqualTo(3L);
        assertThat(result.readingCount()).isEqualTo(4L);
        assertThat(result.completedCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("서재 삭제")
    void delete_userBook() {
        UserBook userBook = UserBook.create(userId, book.getId(), ReadingStatus.READING);

        when(userBookPort.findByUserAndBook(userId, book.getId())).thenReturn(Optional.of(userBook));

        userBookService.execute(new DeleteUserBookCommand(userId.value(), book.getId().getValue()));

        verify(userBookPort).delete(eq(userId), eq(book.getId()));
    }
}
