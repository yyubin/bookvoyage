package org.yyubin.domain.review;

public record Mention(Long mentionedUserId, int startIndex, int endIndex) {
    public Mention {
        if (mentionedUserId == null || mentionedUserId <= 0) {
            throw new IllegalArgumentException("Invalid mentioned user id");
        }
        if (startIndex < 0 || endIndex < startIndex) {
            throw new IllegalArgumentException("Invalid mention indices");
        }
    }
}
