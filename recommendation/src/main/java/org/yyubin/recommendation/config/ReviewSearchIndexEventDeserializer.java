package org.yyubin.recommendation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.apache.kafka.common.serialization.Deserializer;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;

public class ReviewSearchIndexEventDeserializer implements Deserializer<ReviewSearchIndexEvent> {

    private final ObjectMapper objectMapper;

    public ReviewSearchIndexEventDeserializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper = mapper;
    }

    @Override
    public ReviewSearchIndexEvent deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(data, ReviewSearchIndexEvent.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize ReviewSearchIndexEvent", e);
        }
    }
}
