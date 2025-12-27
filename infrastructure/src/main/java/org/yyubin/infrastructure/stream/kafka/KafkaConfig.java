package org.yyubin.infrastructure.stream.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.listener.concurrency:3}")
    private int concurrency;

    @Bean
    public ProducerFactory<String, EventPayload> producerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(
            configs,
            new StringSerializer(),
            new JacksonJsonSerializer<EventPayload>()
        );
    }

    @Bean
    public ProducerFactory<String, ReviewSearchIndexEvent> reviewSearchIndexProducerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(
            configs,
            new StringSerializer(),
            new JacksonJsonSerializer<ReviewSearchIndexEvent>()
        );
    }

    @Bean
    public KafkaTemplate<String, EventPayload> kafkaTemplate(ProducerFactory<String, EventPayload> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, ReviewSearchIndexEvent> reviewSearchIndexKafkaTemplate(
            ProducerFactory<String, ReviewSearchIndexEvent> reviewSearchIndexProducerFactory
    ) {
        return new KafkaTemplate<>(reviewSearchIndexProducerFactory);
    }

    @Bean
    public ConsumerFactory<String, EventPayload> consumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "bookvoyage-default");
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

    /**
     * DLQ (Dead Letter Queue)를 포함한 ErrorHandler
     * 5회 재시도 후 실패한 메시지는 .DLT 토픽으로 전송
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, EventPayload> kafkaTemplate) {
        // DLQ: 실패한 메시지를 {원본토픽}.DLT로 전송
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        // 1초 간격으로 최대 5회 재시도
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 5L));
        errorHandler.setAckAfterHandle(false); // 오류 발생 시 오프셋 커밋하지 않음

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventPayload> kafkaListenerContainerFactory(
            ConsumerFactory<String, EventPayload> consumerFactory,
            DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EventPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency); // 환경변수로 설정 가능
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
