package org.yyubin.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.yyubin.infrastructure.external.book.GoogleBooksProperties;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient googleBooksRestClient(GoogleBooksProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
