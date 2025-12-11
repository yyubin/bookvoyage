package org.yyubin.api.review.dto;

import java.util.List;
import org.yyubin.application.review.dto.PagedCommentResult;

public record CommentPageResponse(
        List<CommentResponse> comments,
        Long nextCursor
) {

    public static CommentPageResponse from(PagedCommentResult result) {
        return new CommentPageResponse(
                result.comments().stream().map(CommentResponse::from).toList(),
                result.nextCursor()
        );
    }
}
