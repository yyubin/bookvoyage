package org.yyubin.domain.review;

public record ReviewLikeId(Long value) {

    public static ReviewLikeId of(Long value) {
        return new ReviewLikeId(value);
    }
}
