package org.yyubin.application.review;

import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.query.GetReviewQuery;

public interface GetReviewUseCase {

    ReviewResult query(GetReviewQuery query);
}
