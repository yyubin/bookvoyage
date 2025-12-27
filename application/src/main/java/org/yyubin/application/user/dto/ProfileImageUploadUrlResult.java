package org.yyubin.application.user.dto;

public record ProfileImageUploadUrlResult(
        String presignedUrl,
        String fileUrl,
        String objectKey
) {
}
