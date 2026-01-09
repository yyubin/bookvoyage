package org.yyubin.infrastructure.recommendation.adapter;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.EmbeddingPort;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "ai.enrichment",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class OpenAIEmbeddingAdapter implements EmbeddingPort {

    private final EmbeddingModel embeddingModel;
    private final int dimension;

    public OpenAIEmbeddingAdapter(
        @Value("${ai.openai.api-key}") String apiKey,
        @Value("${ai.openai.embedding-model:text-embedding-3-small}") String modelName,
        @Value("${ai.openai.embedding-dimension:1536}") int dimension,
        @Value("${ai.openai.timeout:30}") int timeoutSeconds
    ) {
        this.dimension = dimension;

        this.embeddingModel = OpenAiEmbeddingModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(false)
            .logResponses(false)
            .build();

        log.info("OpenAI Embedding Adapter initialized - Model: {}, Dimension: {}",
            modelName, dimension);
    }

    @Override
    public float[] embed(String text) {
        try {
            log.debug("Generating embedding for text: {} chars", text.length());

            var response = embeddingModel.embed(text);
            var embedding = response.content();

            // double[] to float[] 변환
            float[] result = new float[embedding.dimension()];
            for (int i = 0; i < embedding.dimension(); i++) {
                result[i] = (float) embedding.vector()[i];
            }

            log.debug("Embedding generated - Dimension: {}", result.length);
            return result;

        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new RuntimeException("Embedding generation failed", e);
        }
    }

    @Override
    public int getDimension() {
        return dimension;
    }
}
