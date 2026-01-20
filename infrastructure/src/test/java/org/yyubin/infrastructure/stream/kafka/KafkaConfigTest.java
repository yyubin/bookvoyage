package org.yyubin.infrastructure.stream.kafka;

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KafkaConfig 테스트")
class KafkaConfigTest {

    @Test
    @DisplayName("프로듀서/컨슈머 팩토리 설정을 구성한다")
    void config_BuildsFactories() {
        // Given
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "concurrency", 5);

        // When
        DefaultKafkaProducerFactory<String, EventPayload> producerFactory =
                (DefaultKafkaProducerFactory<String, EventPayload>) config.producerFactory();
        DefaultKafkaProducerFactory<String, ReviewSearchIndexEvent> reviewProducerFactory =
                (DefaultKafkaProducerFactory<String, ReviewSearchIndexEvent>) config.reviewSearchIndexProducerFactory();
        DefaultKafkaConsumerFactory<String, EventPayload> consumerFactory =
                (DefaultKafkaConsumerFactory<String, EventPayload>) config.consumerFactory();

        // Then
        Map<String, Object> producerConfigs = producerFactory.getConfigurationProperties();
        assertThat(producerConfigs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(producerConfigs.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(producerConfigs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG)).isEqualTo(true);

        Map<String, Object> reviewProducerConfigs = reviewProducerFactory.getConfigurationProperties();
        assertThat(reviewProducerConfigs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");

        Map<String, Object> consumerConfigs = consumerFactory.getConfigurationProperties();
        assertThat(consumerConfigs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(consumerConfigs.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo("bookvoyage-default");
        assertThat(consumerConfigs.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("latest");
        assertThat(consumerConfigs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
    }

    @Test
    @DisplayName("리스너 팩토리에 동시성 설정을 적용한다")
    void config_ListenerFactory_UsesConcurrency() {
        // Given
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "concurrency", 5);

        KafkaTemplate<String, EventPayload> kafkaTemplate = config.kafkaTemplate(config.producerFactory());
        DefaultErrorHandler errorHandler = config.errorHandler(kafkaTemplate);

        // When
        ConcurrentKafkaListenerContainerFactory<String, EventPayload> factory =
                config.kafkaListenerContainerFactory(config.consumerFactory(), errorHandler);

        // Then
        Integer actualConcurrency = (Integer) ReflectionTestUtils.getField(factory, "concurrency");
        assertThat(actualConcurrency).isEqualTo(5);
    }
}
