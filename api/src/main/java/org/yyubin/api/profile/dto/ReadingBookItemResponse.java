package org.yyubin.api.profile.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.userbook.dto.UserBookResult;

public record ReadingBookItemResponse(
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        int progressPercentage,
        LocalDateTime updatedAt
) {
    public static ReadingBookItemResponse from(UserBookResult result) {
        return new ReadingBookItemResponse(
                result.bookId(),
                result.title(),
                result.authors(),
                result.coverUrl(),
                result.progressPercentage(),
                result.updatedAt()
        );
    }
}
