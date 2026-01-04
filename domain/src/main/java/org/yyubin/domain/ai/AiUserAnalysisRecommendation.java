package org.yyubin.domain.ai;

public record AiUserAnalysisRecommendation(
    Long id,
    Long analysisId,
    Long bookId,
    String bookTitle,
    String author,
    String reason,
    int rank
) {
    public static AiUserAnalysisRecommendation of(
        Long id,
        Long analysisId,
        Long bookId,
        String bookTitle,
        String author,
        String reason,
        int rank
    ) {
        return new AiUserAnalysisRecommendation(
            id,
            analysisId,
            bookId,
            bookTitle,
            author,
            reason,
            rank
        );
    }
}
