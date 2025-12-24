package org.yyubin.recommendation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.beans.factory.annotation.Value;

/**
 * Elasticsearch 설정
 * - 텍스트 기반 콘텐츠 필터링용
 * - Book/Review 인덱싱 및 Semantic Search
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = {
        "org.yyubin.recommendation.search.repository",
        "org.yyubin.recommendation.review.search"
})
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUri;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = elasticsearchUri
                .replace("http://", "")
                .replace("https://", "");

        return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .usingSsl(false)
                .withConnectTimeout(5000)
                .withSocketTimeout(60000)
                .build();
    }
}
