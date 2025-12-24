package org.yyubin.api.userbook.dto;

import org.yyubin.application.userbook.dto.UserBookStatisticsResult;

public record UserBookStatisticsResponse(
        long totalCount,
        long wantToReadCount,
        long readingCount,
        long completedCount
) {
    public static UserBookStatisticsResponse from(UserBookStatisticsResult result) {
        return new UserBookStatisticsResponse(
                result.totalCount(),
                result.wantToReadCount(),
                result.readingCount(),
                result.completedCount()
        );
    }
}
