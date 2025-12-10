package org.yyubin.application.review;

import org.yyubin.application.review.query.GetCommentsQuery;

public interface GetCommentsUseCase {

    PagedCommentResult query(GetCommentsQuery query);
}
