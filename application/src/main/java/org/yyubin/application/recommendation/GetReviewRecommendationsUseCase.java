package org.yyubin.application.recommendation;

import java.util.List;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;
import org.yyubin.application.recommendation.query.GetReviewRecommendationsQuery;

public interface GetReviewRecommendationsUseCase {

    List<ReviewRecommendationResultDto> query(GetReviewRecommendationsQuery query);
}
