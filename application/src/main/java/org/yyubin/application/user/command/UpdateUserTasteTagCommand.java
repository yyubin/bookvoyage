package org.yyubin.application.user.command;

public record UpdateUserTasteTagCommand(
        Long userId,
        String tasteTag
) {
    public UpdateUserTasteTagCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (tasteTag == null) {
            throw new IllegalArgumentException("Taste tag cannot be null");
        }
        if (tasteTag.length() > 100) {
            throw new IllegalArgumentException("Taste tag must not exceed 100 characters");
        }
    }
}
