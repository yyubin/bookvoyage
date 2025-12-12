package org.yyubin.recommendation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Neo4j 설정
 * - 그래프 기반 협업 필터링용
 * - User-Book-Author-Genre-Topic 관계 모델링
 */
@Configuration
@EnableNeo4jRepositories(basePackages = "org.yyubin.recommendation.graph.repository")
@EnableTransactionManagement
public class Neo4jConfig {
}
