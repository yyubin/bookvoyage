package org.yyubin.application.userbook.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.application.userbook.AddUserBookUseCase;
import org.yyubin.application.userbook.DeleteUserBookUseCase;
import org.yyubin.application.userbook.EnsureCompletedUserBookUseCase;
import org.yyubin.application.userbook.GetLatestReadingBooksUseCase;
import org.yyubin.application.userbook.GetUserBookStatisticsUseCase;
import org.yyubin.application.userbook.GetUserBookUseCase;
import org.yyubin.application.userbook.GetUserBooksUseCase;
import org.yyubin.application.userbook.UpdateUserBookMemoUseCase;
import org.yyubin.application.userbook.UpdateUserBookProgressUseCase;
import org.yyubin.application.userbook.UpdateUserBookRatingUseCase;
import org.yyubin.application.userbook.UpdateUserBookStatusUseCase;
import org.yyubin.application.userbook.command.AddUserBookCommand;
import org.yyubin.application.userbook.command.DeleteUserBookCommand;
import org.yyubin.application.userbook.command.EnsureCompletedUserBookCommand;
import org.yyubin.application.userbook.command.UpdateUserBookMemoCommand;
import org.yyubin.application.userbook.command.UpdateUserBookProgressCommand;
import org.yyubin.application.userbook.command.UpdateUserBookRatingCommand;
import org.yyubin.application.userbook.command.UpdateUserBookStatusCommand;
import org.yyubin.application.userbook.dto.UserBookListResult;
import org.yyubin.application.userbook.dto.UserBookResult;
import org.yyubin.application.userbook.dto.UserBookStatisticsResult;
import org.yyubin.application.userbook.port.UserBookPort;
import org.yyubin.application.userbook.port.UserBookQueryPort;
import org.yyubin.application.userbook.query.GetLatestReadingBooksQuery;
import org.yyubin.application.userbook.query.GetUserBookQuery;
import org.yyubin.application.userbook.query.GetUserBookStatisticsQuery;
import org.yyubin.application.userbook.query.GetUserBooksQuery;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.domain.userbook.UserBook;

@Service
@RequiredArgsConstructor
public class UserBookService implements
        AddUserBookUseCase,
        GetUserBooksUseCase,
        GetUserBookUseCase,
        UpdateUserBookStatusUseCase,
        UpdateUserBookProgressUseCase,
        UpdateUserBookRatingUseCase,
        UpdateUserBookMemoUseCase,
        DeleteUserBookUseCase,
        GetUserBookStatisticsUseCase,
        GetLatestReadingBooksUseCase,
        EnsureCompletedUserBookUseCase {

    private final UserBookPort userBookPort;
    private final UserBookQueryPort userBookQueryPort;
    private final LoadBookPort loadBookPort;
    private final SaveBookPort saveBookPort;

    @Override
    @Transactional
    public UserBookResult execute(AddUserBookCommand command) {
        UserId userId = new UserId(command.userId());
        Book book = resolveBook(command.bookSearchItem());
        ReadingStatus status = ReadingStatus.from(command.status());

        if (userBookPort.exists(userId, book.getId())) {
            UserBook existing = userBookPort.findByUserAndBook(userId, book.getId())
                    .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));
            return UserBookResult.from(existing, book);
        }

        UserBook created = userBookPort.save(UserBook.create(userId, book.getId(), status));
        return UserBookResult.from(created, book);
    }

    @Override
    @Transactional(readOnly = true)
    public UserBookListResult query(GetUserBooksQuery query) {
        UserId userId = new UserId(query.userId());
        ReadingStatus filter = resolveStatusFilter(query.status());

        List<UserBook> userBooks = filter == null
                ? userBookPort.findByUser(userId)
                : userBookPort.findByUserAndStatus(userId, filter);

        List<UserBookResult> items = userBooks.stream()
                .map(this::toResult)
                .toList();

        return new UserBookListResult(items);
    }

    @Override
    @Transactional(readOnly = true)
    public UserBookListResult query(GetLatestReadingBooksQuery query) {
        UserId userId = new UserId(query.userId());
        List<UserBook> userBooks = userBookPort.findLatestByUserAndStatus(
                userId,
                ReadingStatus.READING,
                query.size()
        );
        List<UserBookResult> items = userBooks.stream()
                .map(this::toResult)
                .toList();
        return new UserBookListResult(items);
    }

    @Override
    @Transactional(readOnly = true)
    public UserBookResult query(GetUserBookQuery query) {
        UserId userId = new UserId(query.userId());
        UserBook userBook = userBookPort.findByUserAndBook(userId, BookId.of(query.bookId()))
                .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));
        return toResult(userBook);
    }

    @Override
    @Transactional
    public UserBookResult execute(UpdateUserBookStatusCommand command) {
        UserId userId = new UserId(command.userId());
        UserBook userBook = userBookPort.findByUserAndBook(userId, BookId.of(command.bookId()))
                .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));

        ReadingStatus newStatus = ReadingStatus.from(command.status());
        UserBook updated = switch (newStatus) {
            case WANT_TO_READ -> userBook.moveToWantToRead();
            case READING -> userBook.startReading();
            case COMPLETED -> userBook.markAsCompleted();
        };

        return toResult(userBookPort.save(updated));
    }

    @Override
    @Transactional
    public UserBookResult execute(UpdateUserBookProgressCommand command) {
        UserId userId = new UserId(command.userId());
        UserBook userBook = userBookPort.findByUserAndBook(userId, BookId.of(command.bookId()))
                .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));
        return toResult(userBookPort.save(userBook.updateProgress(command.progress())));
    }

    @Override
    @Transactional
    public UserBookResult execute(UpdateUserBookRatingCommand command) {
        UserId userId = new UserId(command.userId());
        UserBook userBook = userBookPort.findByUserAndBook(userId, BookId.of(command.bookId()))
                .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));
        return toResult(userBookPort.save(userBook.rate(command.rating())));
    }

    @Override
    @Transactional
    public UserBookResult execute(UpdateUserBookMemoCommand command) {
        UserId userId = new UserId(command.userId());
        UserBook userBook = userBookPort.findByUserAndBook(userId, BookId.of(command.bookId()))
                .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));
        return toResult(userBookPort.save(userBook.updateMemo(command.memo())));
    }

    @Override
    @Transactional
    public void execute(DeleteUserBookCommand command) {
        UserId userId = new UserId(command.userId());
        userBookPort.findByUserAndBook(userId, BookId.of(command.bookId()))
                .orElseThrow(() -> new IllegalArgumentException("UserBook not found"));
        userBookPort.delete(userId, BookId.of(command.bookId()));
    }

    @Override
    @Transactional
    public void execute(EnsureCompletedUserBookCommand command) {
        UserId userId = new UserId(command.userId());
        BookId bookId = BookId.of(command.bookId());

        UserBook existing = userBookPort.findByUserAndBookIncludingDeleted(userId, bookId)
                .orElse(null);

        if (existing == null) {
            userBookPort.save(UserBook.create(userId, bookId, ReadingStatus.COMPLETED));
            return;
        }

        boolean needsRestore = existing.isDeleted();
        boolean needsCompletion = !existing.isCompleted();

        if (!needsRestore && !needsCompletion) {
            return;
        }

        UserBook updated = existing;
        if (needsRestore) {
            updated = updated.restore();
        }
        if (needsCompletion) {
            updated = updated.markAsCompleted();
        }
        userBookPort.save(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public UserBookStatisticsResult query(GetUserBookStatisticsQuery query) {
        UserId userId = new UserId(query.userId());
        long totalCount = userBookQueryPort.countByUser(userId);
        long wantToReadCount = userBookQueryPort.countByUserAndStatus(userId, ReadingStatus.WANT_TO_READ);
        long readingCount = userBookQueryPort.countByUserAndStatus(userId, ReadingStatus.READING);
        long completedCount = userBookQueryPort.countByUserAndStatus(userId, ReadingStatus.COMPLETED);

        return new UserBookStatisticsResult(
                totalCount,
                wantToReadCount,
                readingCount,
                completedCount
        );
    }

    private UserBookResult toResult(UserBook userBook) {
        Book book = loadBook(userBook.getBookId());
        return UserBookResult.from(userBook, book);
    }

    private Book loadBook(BookId bookId) {
        return loadBookPort.loadById(bookId.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId.getValue()));
    }

    private Book resolveBook(BookSearchItem item) {
        return loadBookPort.loadByIdentifiers(item.getIsbn10(), item.getIsbn13(), item.getGoogleVolumeId())
                .orElseGet(() -> saveBookPort.save(Book.create(
                        item.getTitle(),
                        item.getAuthors(),
                        item.getIsbn10(),
                        item.getIsbn13(),
                        item.getCoverUrl(),
                        item.getPublisher(),
                        item.getPublishedDate(),
                        item.getDescription(),
                        item.getLanguage(),
                        item.getPageCount(),
                        item.getGoogleVolumeId()
                )));
    }

    private ReadingStatus resolveStatusFilter(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return ReadingStatus.from(status);
    }
}
