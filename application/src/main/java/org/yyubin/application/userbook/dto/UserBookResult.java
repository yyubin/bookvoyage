package org.yyubin.application.userbook.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookMetadata;
import org.yyubin.domain.userbook.ReadingStatus;
import org.yyubin.domain.userbook.UserBook;

public record UserBookResult(
        Long userBookId,
        Long bookId,
        String title,
        List<String> authors,
        String isbn10,
        String isbn13,
        String coverUrl,
        String publisher,
        String publishedDate,
        String description,
        String language,
        Integer pageCount,
        String googleVolumeId,
        ReadingStatus status,
        int progressPercentage,
        Integer rating,
        String memo,
        int readingCount,
        LocalDateTime startDate,
        LocalDateTime completionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserBookResult from(UserBook userBook, Book book) {
        BookMetadata metadata = book.getMetadata();
        return new UserBookResult(
                userBook.getId(),
                userBook.getBookId().getValue(),
                metadata.getTitle(),
                metadata.getAuthors(),
                metadata.getIsbn10(),
                metadata.getIsbn13(),
                metadata.getCoverUrl(),
                metadata.getPublisher(),
                metadata.getPublishedDate(),
                metadata.getDescription(),
                metadata.getLanguage(),
                metadata.getPageCount(),
                metadata.getGoogleVolumeId(),
                userBook.getStatus(),
                userBook.getProgress().getPercentage(),
                userBook.getRating().getValue(),
                userBook.getMemo().getContent(),
                userBook.getReadingCount().getCount(),
                userBook.getStartDate(),
                userBook.getCompletionDate(),
                userBook.getCreatedAt(),
                userBook.getUpdatedAt()
        );
    }
}
