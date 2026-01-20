package org.yyubin.infrastructure.external.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.dto.ProfileImageUploadUrlResult;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3ProfileImageStorageAdapter 테스트")
class S3ProfileImageStorageAdapterTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedPutObjectRequest presignedPutObjectRequest;

    private S3Properties s3Properties;
    private S3ProfileImageStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        s3Properties = new S3Properties();
        s3Properties.setBucketName("test-bucket");
        s3Properties.setRegion("ap-northeast-2");
        s3Properties.setPresignedUrlExpirationMinutes(10L);

        adapter = new S3ProfileImageStorageAdapter(s3Presigner, s3Properties);
    }

    @Test
    @DisplayName("presigned URL을 생성한다")
    void generateUploadUrl_CreatesPresignedUrl() throws MalformedURLException {
        // Given
        Long userId = 123L;
        String filename = "profile.jpg";
        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-123.jpg?signature=xxx");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        ProfileImageUploadUrlResult result = adapter.generateUploadUrl(userId, filename);

        // Then
        assertThat(result.presignedUrl()).isEqualTo(presignedUrl.toString());
        assertThat(result.objectKey()).isEqualTo("profile-images/user-123.jpg");
        assertThat(result.fileUrl()).isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-123.jpg");
    }

    @Test
    @DisplayName("파일 확장자가 올바르게 추출된다")
    void generateUploadUrl_ExtractsFileExtension() throws MalformedURLException {
        // Given
        Long userId = 1L;
        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-1.png");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        ProfileImageUploadUrlResult result = adapter.generateUploadUrl(userId, "image.png");

        // Then
        assertThat(result.objectKey()).isEqualTo("profile-images/user-1.png");
    }

    @Test
    @DisplayName("확장자가 없는 파일명도 처리한다")
    void generateUploadUrl_HandlesNoExtension() throws MalformedURLException {
        // Given
        Long userId = 1L;
        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-1");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        ProfileImageUploadUrlResult result = adapter.generateUploadUrl(userId, "filename");

        // Then
        assertThat(result.objectKey()).isEqualTo("profile-images/user-1");
    }

    @Test
    @DisplayName("null 파일명도 처리한다")
    void generateUploadUrl_HandlesNullFilename() throws MalformedURLException {
        // Given
        Long userId = 1L;
        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-1");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        ProfileImageUploadUrlResult result = adapter.generateUploadUrl(userId, null);

        // Then
        assertThat(result.objectKey()).isEqualTo("profile-images/user-1");
    }

    @Test
    @DisplayName("설정된 만료 시간을 사용한다")
    void generateUploadUrl_UsesConfiguredExpiration() throws MalformedURLException {
        // Given
        Long userId = 1L;
        s3Properties.setPresignedUrlExpirationMinutes(15L);
        adapter = new S3ProfileImageStorageAdapter(s3Presigner, s3Properties);

        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-1.jpg");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        adapter.generateUploadUrl(userId, "test.jpg");

        // Then
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());

        assertThat(captor.getValue().signatureDuration()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("올바른 버킷 이름을 사용한다")
    void generateUploadUrl_UsesCorrectBucketName() throws MalformedURLException {
        // Given
        Long userId = 1L;
        URL presignedUrl = new URL("https://my-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-1.jpg");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        adapter.generateUploadUrl(userId, "test.jpg");

        // Then
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());

        assertThat(captor.getValue().putObjectRequest().bucket()).isEqualTo("test-bucket");
    }

    @Test
    @DisplayName("올바른 객체 키를 사용한다")
    void generateUploadUrl_UsesCorrectObjectKey() throws MalformedURLException {
        // Given
        Long userId = 456L;
        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-456.webp");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        adapter.generateUploadUrl(userId, "photo.webp");

        // Then
        ArgumentCaptor<PutObjectPresignRequest> captor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
        verify(s3Presigner).presignPutObject(captor.capture());

        assertThat(captor.getValue().putObjectRequest().key()).isEqualTo("profile-images/user-456.webp");
    }

    @Test
    @DisplayName("fileUrl이 올바른 형식으로 생성된다")
    void generateUploadUrl_FileUrlFormat() throws MalformedURLException {
        // Given
        Long userId = 789L;
        s3Properties.setBucketName("bookvoyage-images");
        s3Properties.setRegion("us-west-2");
        adapter = new S3ProfileImageStorageAdapter(s3Presigner, s3Properties);

        URL presignedUrl = new URL("https://bookvoyage-images.s3.us-west-2.amazonaws.com/profile-images/user-789.gif");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        ProfileImageUploadUrlResult result = adapter.generateUploadUrl(userId, "animation.gif");

        // Then
        assertThat(result.fileUrl()).isEqualTo("https://bookvoyage-images.s3.us-west-2.amazonaws.com/profile-images/user-789.gif");
    }

    @Test
    @DisplayName("다양한 이미지 확장자를 처리한다")
    void generateUploadUrl_HandlesVariousExtensions() throws MalformedURLException {
        // Given
        Long userId = 1L;
        URL presignedUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile-images/user-1.jpeg");

        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);
        when(presignedPutObjectRequest.url()).thenReturn(presignedUrl);

        // When
        ProfileImageUploadUrlResult result = adapter.generateUploadUrl(userId, "photo.jpeg");

        // Then
        assertThat(result.objectKey()).isEqualTo("profile-images/user-1.jpeg");
    }
}
