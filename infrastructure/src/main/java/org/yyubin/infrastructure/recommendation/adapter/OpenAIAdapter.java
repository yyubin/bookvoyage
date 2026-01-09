package org.yyubin.infrastructure.recommendation.adapter;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.LLMPort;

import java.time.Duration;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "ai.enrichment",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class OpenAIAdapter implements LLMPort {

    private final ChatLanguageModel chatModel;
    private final String modelName;
    private final int defaultMaxTokens;

    public OpenAIAdapter(
        @Value("${ai.openai.api-key}") String apiKey,
        @Value("${ai.openai.model:gpt-4o-mini}") String modelName,
        @Value("${ai.openai.max-tokens:500}") int defaultMaxTokens,
        @Value("${ai.openai.temperature:0.7}") double temperature,
        @Value("${ai.openai.timeout:30}") int timeoutSeconds
    ) {
        this.modelName = modelName;
        this.defaultMaxTokens = defaultMaxTokens;

        this.chatModel = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .modelName(modelName)
            .temperature(temperature)
            .timeout(Duration.ofSeconds(timeoutSeconds))
            .logRequests(false)
            .logResponses(false)
            .build();

        log.info("OpenAI Adapter initialized with model: {}", modelName);
    }

    @Override
    public String complete(String prompt) {
        return complete(prompt, defaultMaxTokens);
    }

    @Override
    public String complete(String prompt, int maxTokens) {
        try {
            log.debug("Calling OpenAI API - Model: {}, MaxTokens: {}", modelName, maxTokens);

            String response = chatModel.generate(prompt);

            log.debug("OpenAI response received - Length: {} chars", response.length());
            return response;

        } catch (Exception e) {
            log.error("Failed to call OpenAI API", e);
            throw new RuntimeException("LLM API call failed", e);
        }
    }
}
