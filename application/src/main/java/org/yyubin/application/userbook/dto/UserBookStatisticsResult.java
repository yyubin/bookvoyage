package org.yyubin.application.userbook.dto;

public record UserBookStatisticsResult(
        long totalCount,
        long wantToReadCount,
        long readingCount,
        long completedCount
) {
}
