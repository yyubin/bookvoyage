package org.yyubin.application.user.query;

public record GenerateProfileImageUploadUrlQuery(
        String filename
) {
    public GenerateProfileImageUploadUrlQuery {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or blank");
        }
    }
}
