package org.yyubin.application.recommendation;

import java.util.List;
import org.yyubin.application.recommendation.dto.BookRecommendationResult;
import org.yyubin.application.recommendation.query.GetBookRecommendationsQuery;

public interface GetBookRecommendationsUseCase {

    List<BookRecommendationResult> query(GetBookRecommendationsQuery query);
}
