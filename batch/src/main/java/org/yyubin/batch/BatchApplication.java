package org.yyubin.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.yyubin.batch.config.BatchProperties;

/**
 * Batch Application
 * - 추천 시스템 배치 처리
 * - RDB -> Neo4j/Elasticsearch 동기화
 */
@SpringBootApplication(scanBasePackages = {
        "org.yyubin.batch",
        "org.yyubin.recommendation",
        "org.yyubin.infrastructure",
        "org.yyubin.domain"
})
@EnableConfigurationProperties(BatchProperties.class)
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
