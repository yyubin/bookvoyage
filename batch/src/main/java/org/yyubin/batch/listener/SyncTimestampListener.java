package org.yyubin.batch.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 배치 작업의 마지막 동기화 시각을 Redis에 저장하는 리스너
 * 증분 동기화를 위한 기준 시각 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SyncTimestampListener implements StepExecutionListener {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LAST_SYNC_KEY_PREFIX = "batch:lastSync:";

    @Override
    public @Nullable ExitStatus afterStep(StepExecution stepExecution) {
        String jobName = stepExecution.getJobExecution()
            .getJobInstance().getJobName();
        LocalDateTime syncTime = LocalDateTime.now();

        String key = LAST_SYNC_KEY_PREFIX + jobName;
        redisTemplate.opsForValue().set(key, syncTime.toString());

        log.info("Saved last sync time for job '{}': {}", jobName, syncTime);
        return null;
    }
}
