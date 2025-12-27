package org.yyubin.application.review.search.event;

public interface ReviewSearchIndexEventPublisher {
    void publish(ReviewSearchIndexEvent event);
}
