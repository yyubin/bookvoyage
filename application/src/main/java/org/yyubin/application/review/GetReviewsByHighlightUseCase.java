package org.yyubin.application.review;

import org.yyubin.application.review.dto.PagedReviewResult;
import org.yyubin.application.review.query.GetReviewsByHighlightQuery;

public interface GetReviewsByHighlightUseCase {

    PagedReviewResult query(GetReviewsByHighlightQuery query);
}
