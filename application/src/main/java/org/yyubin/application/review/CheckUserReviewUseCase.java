package org.yyubin.application.review;

import org.yyubin.application.review.query.CheckUserReviewQuery;

public interface CheckUserReviewUseCase {
    boolean query(CheckUserReviewQuery query);
}
