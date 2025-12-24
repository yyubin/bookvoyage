package org.yyubin.application.review.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.ReviewSearchPort;
import org.yyubin.application.review.search.SearchReviewsUseCase;
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
        return reviewSearchPort.search(query.keyword(), query.cursor(), size);
    }

    private int resolveSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }
}
