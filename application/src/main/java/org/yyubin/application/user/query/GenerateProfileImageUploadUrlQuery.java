package org.yyubin.application.user.query;

public record GenerateProfileImageUploadUrlQuery(
        Long userId,
        String filename
) {
    public GenerateProfileImageUploadUrlQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or blank");
        }
    }
}
