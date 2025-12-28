package org.yyubin.api.review.dto;

public record ReviewLikeResponse(
        boolean isLiked,
        long likeCount
) {
}
