package org.yyubin.application.review.query;

public record GetRepliesQuery(
        Long parentCommentId,
        Long viewerId,
        Long cursor,
        int size
) {
}
