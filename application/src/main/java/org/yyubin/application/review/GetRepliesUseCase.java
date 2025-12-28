package org.yyubin.application.review;

import org.yyubin.application.review.dto.PagedCommentResult;
import org.yyubin.application.review.query.GetRepliesQuery;

public interface GetRepliesUseCase {

    PagedCommentResult query(GetRepliesQuery query);
}
