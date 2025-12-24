package org.yyubin.application.review.port;

import org.yyubin.application.review.search.dto.ReviewSearchPageResult;

public interface ReviewSearchPort {
    ReviewSearchPageResult search(String keyword, Long cursor, int size);
}
