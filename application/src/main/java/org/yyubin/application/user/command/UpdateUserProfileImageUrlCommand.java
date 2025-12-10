package org.yyubin.application.user.command;

public record UpdateUserProfileImageUrlCommand(
        Long userId,
        String newProfileImageUrl
) {
    public UpdateUserProfileImageUrlCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (newProfileImageUrl == null) {
            throw new IllegalArgumentException("ProfileImageUrl cannot be null");
        }
    }
}
