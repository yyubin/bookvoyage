package org.yyubin.api.userbook.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.userbook.dto.UserBookResult;
import org.yyubin.domain.userbook.ReadingStatus;

public record UserBookResponse(
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
    public static UserBookResponse from(UserBookResult result) {
        return new UserBookResponse(
                result.userBookId(),
                result.bookId(),
                result.title(),
                result.authors(),
                result.isbn10(),
                result.isbn13(),
                result.coverUrl(),
                result.publisher(),
                result.publishedDate(),
                result.description(),
                result.language(),
                result.pageCount(),
                result.googleVolumeId(),
                result.status(),
                result.progressPercentage(),
                result.rating(),
                result.memo(),
                result.readingCount(),
                result.startDate(),
                result.completionDate(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
