package org.yyubin.application.review.search;

import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.application.review.search.query.SearchReviewsQuery;

public interface SearchReviewsUseCase {
    ReviewSearchPageResult query(SearchReviewsQuery query);
}
