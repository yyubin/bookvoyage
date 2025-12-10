package org.yyubin.application.user.command;

public record UpdateUserBioCommand(
        Long userId,
        String bio
) {
    public UpdateUserBioCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (bio == null) {
            throw new IllegalArgumentException("Bio cannot be null");
        }
        if (bio.length() > 500) {
            throw new IllegalArgumentException("Bio must not exceed 500 characters");
        }
    }
}
