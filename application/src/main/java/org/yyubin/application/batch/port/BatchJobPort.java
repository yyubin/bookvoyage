package org.yyubin.application.batch.port;

import org.yyubin.application.batch.dto.BatchJobResult;

public interface BatchJobPort {

    /**
     * 배치 작업 실행
     *
     * @param jobName 실행할 배치 작업 이름
     * @return 배치 작업 실행 결과
     */
    BatchJobResult runJob(String jobName);

    /**
     * 배치 작업이 현재 실행 중인지 확인
     *
     * @param jobName 확인할 배치 작업 이름
     * @return 실행 중이면 true
     */
    boolean isJobRunning(String jobName);
}
