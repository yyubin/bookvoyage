package org.yyubin.application.batch;

import org.yyubin.application.batch.dto.BatchJobResult;

public interface TriggerBatchJobUseCase {

    /**
     * 배치 작업을 수동으로 트리거
     *
     * @param jobName 실행할 배치 작업 이름
     * @return 배치 작업 실행 결과
     */
    BatchJobResult trigger(String jobName);
}
