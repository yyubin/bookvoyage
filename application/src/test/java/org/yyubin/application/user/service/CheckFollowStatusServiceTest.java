package org.yyubin.application.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.user.dto.CheckFollowStatusResult;
import org.yyubin.application.user.port.FollowPort;
import org.yyubin.application.user.query.CheckFollowStatusQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckFollowStatusService 테스트")
class CheckFollowStatusServiceTest {

    @Mock
    private FollowPort followPort;

    @InjectMocks
    private CheckFollowStatusService checkFollowStatusService;

    @Nested
    @DisplayName("check 메서드")
    class CheckMethod {

        @Test
        @DisplayName("팔로우 중인 경우 true 반환")
        void check_ReturnsTrue_WhenFollowing() {
            // Given
            CheckFollowStatusQuery query = new CheckFollowStatusQuery(1L, 2L);
            when(followPort.exists(1L, 2L)).thenReturn(true);

            // When
            CheckFollowStatusResult result = checkFollowStatusService.check(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.following()).isTrue();

            verify(followPort).exists(1L, 2L);
        }

        @Test
        @DisplayName("팔로우하지 않는 경우 false 반환")
        void check_ReturnsFalse_WhenNotFollowing() {
            // Given
            CheckFollowStatusQuery query = new CheckFollowStatusQuery(1L, 2L);
            when(followPort.exists(1L, 2L)).thenReturn(false);

            // When
            CheckFollowStatusResult result = checkFollowStatusService.check(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.following()).isFalse();

            verify(followPort).exists(1L, 2L);
        }

        @Test
        @DisplayName("본인 프로필 조회 시 항상 false 반환")
        void check_ReturnsFalse_WhenCheckingOwnProfile() {
            // Given
            CheckFollowStatusQuery query = new CheckFollowStatusQuery(1L, 1L);

            // When
            CheckFollowStatusResult result = checkFollowStatusService.check(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.following()).isFalse();

            // 본인 프로필일 때는 DB 조회하지 않음
            verify(followPort, never()).exists(1L, 1L);
        }

        @Test
        @DisplayName("다른 사용자의 팔로우 상태 확인")
        void check_DifferentUsers() {
            // Given
            CheckFollowStatusQuery query1 = new CheckFollowStatusQuery(10L, 20L);
            CheckFollowStatusQuery query2 = new CheckFollowStatusQuery(20L, 10L);

            when(followPort.exists(10L, 20L)).thenReturn(true);
            when(followPort.exists(20L, 10L)).thenReturn(false);

            // When
            CheckFollowStatusResult result1 = checkFollowStatusService.check(query1);
            CheckFollowStatusResult result2 = checkFollowStatusService.check(query2);

            // Then
            assertThat(result1.following()).isTrue();
            assertThat(result2.following()).isFalse();

            verify(followPort).exists(10L, 20L);
            verify(followPort).exists(20L, 10L);
        }
    }

    @Nested
    @DisplayName("CheckFollowStatusQuery 테스트")
    class QueryTests {

        @Test
        @DisplayName("쿼리 생성 성공")
        void query_CreateSuccess() {
            // When
            CheckFollowStatusQuery query = new CheckFollowStatusQuery(1L, 2L);

            // Then
            assertThat(query.followerId()).isEqualTo(1L);
            assertThat(query.targetUserId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("null 값도 허용 (유효성 검사 없음)")
        void query_AllowsNull() {
            // When
            CheckFollowStatusQuery query = new CheckFollowStatusQuery(null, null);

            // Then
            assertThat(query.followerId()).isNull();
            assertThat(query.targetUserId()).isNull();
        }
    }

    @Nested
    @DisplayName("CheckFollowStatusResult 테스트")
    class ResultTests {

        @Test
        @DisplayName("following true 결과")
        void result_FollowingTrue() {
            // When
            CheckFollowStatusResult result = new CheckFollowStatusResult(true);

            // Then
            assertThat(result.following()).isTrue();
        }

        @Test
        @DisplayName("following false 결과")
        void result_FollowingFalse() {
            // When
            CheckFollowStatusResult result = new CheckFollowStatusResult(false);

            // Then
            assertThat(result.following()).isFalse();
        }
    }
}
