package org.yyubin.api.review.dto;

public record MentionResponse(
        Long mentionedUserId,
        int startIndex,
        int endIndex
) {
    public static MentionResponse from(org.yyubin.domain.review.Mention mention) {
        return new MentionResponse(mention.mentionedUserId(), mention.startIndex(), mention.endIndex());
    }
}
