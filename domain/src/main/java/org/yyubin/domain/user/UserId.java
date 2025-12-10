package org.yyubin.domain.user;

public record UserId(Long value) {
    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
}
