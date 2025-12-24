package org.yyubin.application.profile.query;

public record GetProfileSummaryQuery(Long userId) {
    public GetProfileSummaryQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
    }
}
