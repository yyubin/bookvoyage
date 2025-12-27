package org.yyubin.recommendation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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

    @Override
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        return new ElasticsearchCustomConversions(List.of(
                new LocalDateTimeToStringConverter(),
                new StringToLocalDateTimeConverter()
        ));
    }

    @WritingConverter
    private static class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        @Override
        public String convert(LocalDateTime source) {
            if (source == null) {
                return null;
            }
            return source.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    @ReadingConverter
    private static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            try {
                return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ignored) {
                // date-only fallback
            }
            try {
                return LocalDate.parse(source, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

}
