package org.yyubin.infrastructure.book.trending;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.book.GetShelfAdditionTrendUseCase;
import org.yyubin.application.book.query.GetShelfAdditionTrendQuery;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShelfAdditionTrendScheduler 테스트")
class ShelfAdditionTrendSchedulerTest {

    @Mock
    private GetShelfAdditionTrendUseCase getShelfAdditionTrendUseCase;

    @InjectMocks
    private ShelfAdditionTrendScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "enabled", true);
        ReflectionTestUtils.setField(scheduler, "timezone", "Asia/Seoul");
        ReflectionTestUtils.setField(scheduler, "defaultLimit", 20);
    }

    @Test
    @DisplayName("활성화되면 캐시를 새로고침한다")
    void refreshTodayCache_Enabled_RefreshesCache() {
        // When
        scheduler.refreshTodayCache();

        // Then
        ArgumentCaptor<GetShelfAdditionTrendQuery> queryCaptor =
            ArgumentCaptor.forClass(GetShelfAdditionTrendQuery.class);
        verify(getShelfAdditionTrendUseCase).query(queryCaptor.capture());

        GetShelfAdditionTrendQuery query = queryCaptor.getValue();
        assertThat(query.timezone()).isEqualTo(ZoneId.of("Asia/Seoul"));
        assertThat(query.limit()).isEqualTo(20);
        assertThat(query.forceRefresh()).isTrue();
    }

    @Test
    @DisplayName("비활성화되면 아무것도 하지 않는다")
    void refreshTodayCache_Disabled_DoesNothing() {
        // Given
        ReflectionTestUtils.setField(scheduler, "enabled", false);

        // When
        scheduler.refreshTodayCache();

        // Then
        verify(getShelfAdditionTrendUseCase, never()).query(any());
    }

    @Test
    @DisplayName("예외 발생 시 로그만 남기고 계속 진행한다")
    void refreshTodayCache_OnException_LogsAndContinues() {
        // Given
        doThrow(new RuntimeException("DB error")).when(getShelfAdditionTrendUseCase).query(any());

        // When - 예외가 발생해도 테스트가 통과해야 함
        scheduler.refreshTodayCache();

        // Then
        verify(getShelfAdditionTrendUseCase).query(any());
    }

    @Test
    @DisplayName("설정된 타임존을 사용한다")
    void refreshTodayCache_UsesConfiguredTimezone() {
        // Given
        ReflectionTestUtils.setField(scheduler, "timezone", "America/New_York");

        // When
        scheduler.refreshTodayCache();

        // Then
        ArgumentCaptor<GetShelfAdditionTrendQuery> queryCaptor =
            ArgumentCaptor.forClass(GetShelfAdditionTrendQuery.class);
        verify(getShelfAdditionTrendUseCase).query(queryCaptor.capture());

        assertThat(queryCaptor.getValue().timezone()).isEqualTo(ZoneId.of("America/New_York"));
    }

    @Test
    @DisplayName("설정된 limit을 사용한다")
    void refreshTodayCache_UsesConfiguredLimit() {
        // Given
        ReflectionTestUtils.setField(scheduler, "defaultLimit", 50);

        // When
        scheduler.refreshTodayCache();

        // Then
        ArgumentCaptor<GetShelfAdditionTrendQuery> queryCaptor =
            ArgumentCaptor.forClass(GetShelfAdditionTrendQuery.class);
        verify(getShelfAdditionTrendUseCase).query(queryCaptor.capture());

        assertThat(queryCaptor.getValue().limit()).isEqualTo(50);
    }

    @Test
    @DisplayName("오늘 날짜를 기준으로 쿼리한다")
    void refreshTodayCache_QueriesTodayDate() {
        // When
        scheduler.refreshTodayCache();

        // Then
        ArgumentCaptor<GetShelfAdditionTrendQuery> queryCaptor =
            ArgumentCaptor.forClass(GetShelfAdditionTrendQuery.class);
        verify(getShelfAdditionTrendUseCase).query(queryCaptor.capture());

        // 오늘 날짜가 설정되어 있는지 확인 (null이 아님)
        assertThat(queryCaptor.getValue().date()).isNotNull();
    }
}
