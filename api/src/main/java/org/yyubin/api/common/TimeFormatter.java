package org.yyubin.api.common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        long seconds = duration.getSeconds();
        if (seconds < 0) {
            return "방금 전";
        }

        // 1분 미만
        if (seconds < 60) {
            return "방금 전";
        }

        // 1시간 미만
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "분 전";
        }

        // 1일 미만
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }

        // 1주 미만
        long days = hours / 24;
        if (days < 7) {
            return days + "일 전";
        }

        // 1개월 미만 (약 30일)
        if (days < 30) {
            long weeks = days / 7;
            return weeks + "주 전";
        }

        // 1년 미만
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) {
            return months + "개월 전";
        }

        // 1년 이상
        long years = ChronoUnit.YEARS.between(dateTime, now);
        return years + "년 전";
    }
}
