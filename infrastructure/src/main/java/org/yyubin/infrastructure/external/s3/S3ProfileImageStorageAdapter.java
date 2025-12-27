package org.yyubin.infrastructure.external.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.application.user.dto.ProfileImageUploadUrlResult;
import org.yyubin.application.user.port.ProfileImageStoragePort;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ProfileImageStorageAdapter implements ProfileImageStoragePort {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Override
    public ProfileImageUploadUrlResult generateUploadUrl(Long userId, String originalFilename) {
        String fileExtension = extractFileExtension(originalFilename);
        String objectKey = generateObjectKey(userId, fileExtension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(objectKey)
                .contentType(determineContentType(fileExtension))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(s3Properties.getPresignedUrlExpirationMinutes()))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();
        String fileUrl = buildFileUrl(objectKey);

        log.info("Generated presigned URL for upload: objectKey={}, expiresIn={}min",
                objectKey, s3Properties.getPresignedUrlExpirationMinutes());

        return new ProfileImageUploadUrlResult(presignedUrl, fileUrl, objectKey);
    }

    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateObjectKey(Long userId, String extension) {
        return String.format("profile-images/user-%d%s", userId, extension);
    }

    private String determineContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private String buildFileUrl(String objectKey) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.getBucketName(),
                s3Properties.getRegion(),
                objectKey);
    }
}
