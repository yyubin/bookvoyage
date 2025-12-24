package org.yyubin.recommendation.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.yyubin.application.event.EventPayload;

@Configuration
@EnableKafka
public class RecommendationKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:cg-review-recommendation}")
    private String groupId;

    @Value("${spring.kafka.listener.concurrency:2}")
    private int concurrency;

    @Bean
    public ConsumerFactory<String, EventPayload> recommendationConsumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        JacksonJsonDeserializer<EventPayload> jsonDeserializer = new JacksonJsonDeserializer<>(EventPayload.class);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                configs,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public DefaultErrorHandler recommendationErrorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1000L, 3L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventPayload> recommendationKafkaListenerContainerFactory(
            ConsumerFactory<String, EventPayload> recommendationConsumerFactory,
            DefaultErrorHandler recommendationErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EventPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(recommendationConsumerFactory);
        factory.setConcurrency(concurrency);
        factory.setCommonErrorHandler(recommendationErrorHandler);
        return factory;
    }
}
