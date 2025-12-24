package org.yyubin.recommendation.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "recommendationTaskExecutor")
    public Executor recommendationTaskExecutor(
            @Value("${recommendation.async.core-pool-size:4}") int corePoolSize,
            @Value("${recommendation.async.max-pool-size:8}") int maxPoolSize,
            @Value("${recommendation.async.queue-capacity:500}") int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("recommendation-async-");
        executor.initialize();
        return executor;
    }
}
