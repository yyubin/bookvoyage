package org.yyubin.api.user.dto;

public record ProfileImageUploadResponse(
        String presignedUrl,
        String fileUrl,
        String objectKey
) {
    public static ProfileImageUploadResponse of(String presignedUrl, String fileUrl, String objectKey) {
        return new ProfileImageUploadResponse(presignedUrl, fileUrl, objectKey);
    }
}
