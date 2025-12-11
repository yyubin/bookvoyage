package org.yyubin.application.event;

public interface EventPublisher {
    void publish(String topic, String key, EventPayload payload);
}
