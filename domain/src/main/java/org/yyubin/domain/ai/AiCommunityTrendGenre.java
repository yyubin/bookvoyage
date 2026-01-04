package org.yyubin.domain.ai;

public record AiCommunityTrendGenre(
    String genre,
    Double percentage,
    String mood
) {
    public static AiCommunityTrendGenre of(
        String genre,
        Double percentage,
        String mood
    ) {
        return new AiCommunityTrendGenre(genre, percentage, mood);
    }
}
