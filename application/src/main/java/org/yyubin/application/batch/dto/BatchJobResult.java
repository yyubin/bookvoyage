package org.yyubin.application.batch.dto;

public record BatchJobResult(
        String jobName,
        Long executionId,
        String status,
        String message
) {
    public static BatchJobResult success(String jobName, Long executionId) {
        return new BatchJobResult(jobName, executionId, "STARTED", "Job started successfully");
    }

    public static BatchJobResult alreadyRunning(String jobName) {
        return new BatchJobResult(jobName, null, "ALREADY_RUNNING", "Job is already running");
    }

    public static BatchJobResult error(String jobName, String message) {
        return new BatchJobResult(jobName, null, "ERROR", message);
    }
}
