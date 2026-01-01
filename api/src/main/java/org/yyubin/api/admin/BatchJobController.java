package org.yyubin.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.application.batch.TriggerBatchJobUseCase;
import org.yyubin.application.batch.dto.BatchJobResult;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class BatchJobController {

    private final TriggerBatchJobUseCase triggerBatchJobUseCase;

    /**
     * Elasticsearch 동기화 배치 작업 실행
     * - 책(Book) 및 리뷰(Review) 데이터를 Elasticsearch에 동기화
     * - 신규 등록된 책이 검색에 나오지 않을 때 사용
     */
    @PostMapping("/sync-elasticsearch")
    public ResponseEntity<Map<String, Object>> syncElasticsearch() {
        log.info("Triggering Elasticsearch sync batch job via API");
        BatchJobResult result = triggerBatchJobUseCase.trigger("elasticsearchSyncJob");
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * Neo4j 동기화 배치 작업 실행
     * - 추천 시스템용 그래프 데이터베이스 동기화
     */
    @PostMapping("/sync-neo4j")
    public ResponseEntity<Map<String, Object>> syncNeo4j() {
        log.info("Triggering Neo4j sync batch job via API");
        BatchJobResult result = triggerBatchJobUseCase.trigger("neo4jSyncJob");
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * 리뷰 조회수 플러시 배치 작업 실행
     * - Redis에 쌓인 리뷰 조회수를 DB로 플러시
     */
    @PostMapping("/flush-review-views")
    public ResponseEntity<Map<String, Object>> flushReviewViews() {
        log.info("Triggering review view flush batch job via API");
        BatchJobResult result = triggerBatchJobUseCase.trigger("reviewViewFlushJob");
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * 리뷰 콘텐츠 동기화 배치 작업 실행
     * - 추천 시스템용 리뷰 콘텐츠 동기화
     */
    @PostMapping("/sync-review-content")
    public ResponseEntity<Map<String, Object>> syncReviewContent() {
        log.info("Triggering review content sync batch job via API");
        BatchJobResult result = triggerBatchJobUseCase.trigger("reviewContentSyncJob");
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * 범용 배치 작업 트리거 (특정 작업명 지정)
     *
     * @param jobName 실행할 배치 작업 이름
     *                - elasticsearchSyncJob
     *                - neo4jSyncJob
     *                - reviewViewFlushJob
     *                - reviewContentSyncJob
     */
    @PostMapping("/trigger/{jobName}")
    public ResponseEntity<Map<String, Object>> triggerJob(@PathVariable String jobName) {
        log.info("Triggering batch job {} via API", jobName);
        BatchJobResult result = triggerBatchJobUseCase.trigger(jobName);
        return ResponseEntity.ok(toResponse(result));
    }

    private Map<String, Object> toResponse(BatchJobResult result) {
        return Map.of(
                "jobName", result.jobName(),
                "status", result.status(),
                "message", result.message(),
                "executionId", result.executionId() != null ? result.executionId() : "N/A"
        );
    }
}
