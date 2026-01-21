package org.yyubin.batch.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.batch.runner.BatchJobRunner;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchScheduler 테스트")
class BatchSchedulerTest {

    @Mock
    private BatchJobRunner batchJobRunner;

    @InjectMocks
    private BatchScheduler batchScheduler;

    @Test
    @DisplayName("Neo4j 동기화 Job 실행")
    void syncNeo4j_RunsCorrectJob() {
        // When
        batchScheduler.syncNeo4j();

        // Then
        verify(batchJobRunner).run("neo4jSyncJob");
    }

    @Test
    @DisplayName("Elasticsearch 동기화 Job 실행")
    void syncElasticsearch_RunsCorrectJob() {
        // When
        batchScheduler.syncElasticsearch();

        // Then
        verify(batchJobRunner).run("elasticsearchSyncJob");
    }

    @Test
    @DisplayName("리뷰 조회수 플러시 Job 실행")
    void flushReviewViews_RunsCorrectJob() {
        // When
        batchScheduler.flushReviewViews();

        // Then
        verify(batchJobRunner).run("reviewViewFlushJob");
    }

    @Test
    @DisplayName("리뷰 컨텐츠 동기화 Job 실행")
    void syncReviewContent_RunsCorrectJob() {
        // When
        batchScheduler.syncReviewContent();

        // Then
        verify(batchJobRunner).run("reviewContentSyncJob");
    }

    @Test
    @DisplayName("검색 쿼리 로그 플러시 Job 실행")
    void flushSearchQueryLogs_RunsCorrectJob() {
        // When
        batchScheduler.flushSearchQueryLogs();

        // Then
        verify(batchJobRunner).run("searchQueryLogFlushJob");
    }
}
