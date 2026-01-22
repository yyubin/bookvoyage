package org.yyubin.application.user.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.dto.ProfileImageUploadUrlResult;
import org.yyubin.application.user.port.ProfileImageStoragePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateProfileImageUploadUrlQueryHandler 테스트")
class GenerateProfileImageUploadUrlQueryHandlerTest {

    @Mock
    private ProfileImageStoragePort profileImageStoragePort;

    @InjectMocks
    private GenerateProfileImageUploadUrlQueryHandler handler;

    @Nested
    @DisplayName("handle 메서드")
    class HandleMethod {

        @Test
        @DisplayName("프로필 이미지 업로드 URL 생성 성공")
        void handle_Success() {
            // Given
            GenerateProfileImageUploadUrlQuery query = new GenerateProfileImageUploadUrlQuery(1L, "profile.jpg");
            ProfileImageUploadUrlResult expectedResult = new ProfileImageUploadUrlResult(
                    "https://s3.amazonaws.com/bucket/presigned-url",
                    "https://s3.amazonaws.com/bucket/users/1/profile.jpg",
                    "users/1/profile.jpg"
            );
            when(profileImageStoragePort.generateUploadUrl(1L, "profile.jpg")).thenReturn(expectedResult);

            // When
            ProfileImageUploadUrlResult result = handler.handle(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.presignedUrl()).isEqualTo("https://s3.amazonaws.com/bucket/presigned-url");
            assertThat(result.fileUrl()).isEqualTo("https://s3.amazonaws.com/bucket/users/1/profile.jpg");
            assertThat(result.objectKey()).isEqualTo("users/1/profile.jpg");

            verify(profileImageStoragePort).generateUploadUrl(1L, "profile.jpg");
        }

        @Test
        @DisplayName("PNG 이미지 업로드 URL 생성 성공")
        void handle_Success_PngImage() {
            // Given
            GenerateProfileImageUploadUrlQuery query = new GenerateProfileImageUploadUrlQuery(2L, "avatar.png");
            ProfileImageUploadUrlResult expectedResult = new ProfileImageUploadUrlResult(
                    "https://s3.amazonaws.com/bucket/presigned-url-png",
                    "https://s3.amazonaws.com/bucket/users/2/avatar.png",
                    "users/2/avatar.png"
            );
            when(profileImageStoragePort.generateUploadUrl(2L, "avatar.png")).thenReturn(expectedResult);

            // When
            ProfileImageUploadUrlResult result = handler.handle(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.objectKey()).isEqualTo("users/2/avatar.png");

            verify(profileImageStoragePort).generateUploadUrl(2L, "avatar.png");
        }

        @Test
        @DisplayName("한글 파일명으로 업로드 URL 생성 성공")
        void handle_Success_KoreanFilename() {
            // Given
            GenerateProfileImageUploadUrlQuery query = new GenerateProfileImageUploadUrlQuery(3L, "프로필사진.jpg");
            ProfileImageUploadUrlResult expectedResult = new ProfileImageUploadUrlResult(
                    "https://s3.amazonaws.com/bucket/presigned-url-korean",
                    "https://s3.amazonaws.com/bucket/users/3/프로필사진.jpg",
                    "users/3/프로필사진.jpg"
            );
            when(profileImageStoragePort.generateUploadUrl(3L, "프로필사진.jpg")).thenReturn(expectedResult);

            // When
            ProfileImageUploadUrlResult result = handler.handle(query);

            // Then
            assertThat(result).isNotNull();
            verify(profileImageStoragePort).generateUploadUrl(3L, "프로필사진.jpg");
        }

        @Test
        @DisplayName("스토리지 포트 예외 발생 시 전파")
        void handle_PropagatesStorageException() {
            // Given
            GenerateProfileImageUploadUrlQuery query = new GenerateProfileImageUploadUrlQuery(1L, "profile.jpg");
            when(profileImageStoragePort.generateUploadUrl(1L, "profile.jpg"))
                    .thenThrow(new RuntimeException("S3 connection failed"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(query))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("S3 connection failed");
        }
    }

    @Nested
    @DisplayName("GenerateProfileImageUploadUrlQuery 유효성 검사")
    class QueryValidation {

        @Test
        @DisplayName("null userId로 쿼리 생성 시 예외 발생")
        void query_ThrowsExceptionForNullUserId() {
            assertThatThrownBy(() -> new GenerateProfileImageUploadUrlQuery(null, "profile.jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");
        }

        @Test
        @DisplayName("0 이하의 userId로 쿼리 생성 시 예외 발생")
        void query_ThrowsExceptionForInvalidUserId() {
            assertThatThrownBy(() -> new GenerateProfileImageUploadUrlQuery(0L, "profile.jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");

            assertThatThrownBy(() -> new GenerateProfileImageUploadUrlQuery(-1L, "profile.jpg"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");
        }

        @Test
        @DisplayName("null filename으로 쿼리 생성 시 예외 발생")
        void query_ThrowsExceptionForNullFilename() {
            assertThatThrownBy(() -> new GenerateProfileImageUploadUrlQuery(1L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Filename cannot be null or blank");
        }

        @Test
        @DisplayName("빈 filename으로 쿼리 생성 시 예외 발생")
        void query_ThrowsExceptionForBlankFilename() {
            assertThatThrownBy(() -> new GenerateProfileImageUploadUrlQuery(1L, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Filename cannot be null or blank");

            assertThatThrownBy(() -> new GenerateProfileImageUploadUrlQuery(1L, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Filename cannot be null or blank");
        }

        @Test
        @DisplayName("유효한 쿼리 생성 성공")
        void query_Success() {
            GenerateProfileImageUploadUrlQuery query = new GenerateProfileImageUploadUrlQuery(1L, "profile.jpg");

            assertThat(query.userId()).isEqualTo(1L);
            assertThat(query.filename()).isEqualTo("profile.jpg");
        }
    }
}
