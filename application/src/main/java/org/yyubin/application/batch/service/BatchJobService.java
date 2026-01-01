package org.yyubin.application.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.application.batch.TriggerBatchJobUseCase;
import org.yyubin.application.batch.dto.BatchJobResult;
import org.yyubin.application.batch.port.BatchJobPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService implements TriggerBatchJobUseCase {

    private final BatchJobPort batchJobPort;

    @Override
    public BatchJobResult trigger(String jobName) {
        log.info("Triggering batch job: {}", jobName);

        if (batchJobPort.isJobRunning(jobName)) {
            log.warn("Batch job {} is already running", jobName);
            return BatchJobResult.alreadyRunning(jobName);
        }

        return batchJobPort.runJob(jobName);
    }
}
