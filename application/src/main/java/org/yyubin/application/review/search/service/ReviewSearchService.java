package org.yyubin.application.review.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.ReviewSearchPort;
import org.yyubin.application.review.search.SearchReviewsUseCase;
import org.yyubin.application.review.search.dto.ReviewSearchFilter;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.application.review.search.query.SearchReviewsQuery;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewSearchService implements SearchReviewsUseCase {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 30;

    private final ReviewSearchPort reviewSearchPort;

    @Override
    public ReviewSearchPageResult query(SearchReviewsQuery query) {
        int size = resolveSize(query.size());

        // 필터가 있으면 새 메서드 사용, 없으면 기존 메서드 사용
        if (hasFilters(query)) {
            ReviewSearchFilter filter = ReviewSearchFilter.from(query, size);
            return reviewSearchPort.searchWithFilters(filter);
        } else {
            return reviewSearchPort.search(query.keyword(), query.cursor(), size);
        }
    }

    private boolean hasFilters(SearchReviewsQuery query) {
        return query.genre() != null
                || query.minRating() != null
                || query.maxRating() != null
                || query.startDate() != null
                || query.endDate() != null
                || query.highlight() != null
                || query.bookId() != null
                || query.userId() != null
                || query.sortBy() != null;
    }

    private int resolveSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }
}
