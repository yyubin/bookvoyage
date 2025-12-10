package org.yyubin.application.review;

import org.yyubin.application.review.query.GetUserReviewsQuery;

public interface GetUserReviewsUseCase {

    PagedReviewResult query(GetUserReviewsQuery query);
}
