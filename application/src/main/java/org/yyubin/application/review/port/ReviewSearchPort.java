package org.yyubin.application.review.port;

import org.yyubin.application.review.search.dto.ReviewSearchFilter;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;

public interface ReviewSearchPort {
    // 하위 호환성을 위한 기존 메서드
    ReviewSearchPageResult search(String keyword, Long cursor, int size);

    // 필터링을 지원하는 새 메서드
    ReviewSearchPageResult searchWithFilters(ReviewSearchFilter filter);
}
